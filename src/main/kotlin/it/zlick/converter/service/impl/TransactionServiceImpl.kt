package it.zlick.converter.service.impl

import it.zlick.converter.model.Transaction
import it.zlick.converter.service.Result
import it.zlick.converter.service.Summary
import it.zlick.converter.service.TransactionConverter
import it.zlick.converter.service.TransactionService
import it.zlick.converter.service.external.TransactionProcessor
import it.zlick.converter.service.external.TransactionProvider
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.allOf
import java.util.concurrent.CompletableFuture.supplyAsync

@Service
class TransactionServiceImpl(
  private val provider: TransactionProvider,
  private val converter: TransactionConverter,
  private val processor: TransactionProcessor
  ): TransactionService {

  override fun process(n: Int, chunkSize: Int, targetCurrency: String): Summary {
    val fetchAsyncs = fetchTransactions(n)
    val convertAsyncs = convertTransactions(fetchAsyncs, targetCurrency)

    allOf(*convertAsyncs.toTypedArray()).thenApply {
      convertAsyncs.map { it.join() }
    }

    val fetched = fetchAsyncs.map { it.get() }.filterNotNull()
    val converted = convertAsyncs.map { it.get() }.filterNotNull()
    val notConverted = fetched.filter { it.checksum !in converted.map { it.checksum } }

    val processAsyncs = processTransactions(converted, chunkSize=10)

    allOf(*processAsyncs.toTypedArray()).thenApply {
      processAsyncs.map { it.join() }
    }

    val processingResults = processAsyncs.map { it.get() }

    return Summary(
      expected = n,
      fetched = fetched.size,
      conversion = Result(
        expected = fetched.size,
        successful = converted.size,
        failed = fetched.size - converted.size,
        failures = notConverted
      ),
      processing = processingResults.reduce { acc, result ->
        acc.copy(
          expected = acc.expected + result.expected,
          successful = acc.successful + result.successful,
          failed = acc.failed + result.failed,
          failures = acc.failures.plus(result.failures)
        )
      }
    )
  }

  private fun fetchTransactions(n: Int): List<CompletableFuture<Transaction?>> {
    return (1..n).map {
      supplyAsync {
        runCatching {
          provider.getTransaction()
        }.recover {
          LOG.error("Transaction could not be fetched: ${it.message}")
          null
        }.getOrNull()
      }
    }
  }

  private fun convertTransactions(transactions: List<CompletableFuture<Transaction?>>, targetCurrency: String): List<CompletableFuture<Transaction?>> {
    return transactions.map {
      if(it != null) {
        it.thenCompose {
          supplyAsync {
            runCatching {
              converter.convert(it!!, targetCurrency)
            }.recover {
              LOG.error("Transaction could not be converted: ${it.message}")
              null
            }.getOrNull()
          }
        }
      } else {
        CompletableFuture.completedFuture(null)
      }
    }
  }

  private fun processTransactions(list: List<Transaction>, chunkSize: Int): List<CompletableFuture<Result>> {
    return list.chunked(chunkSize) { chunk ->
      supplyAsync {
        runCatching {
          val result = processor.process(chunk)
          Result(expected = chunk.size, successful = chunk.size, failed = 0, failures = emptyList())
        }.recover {
          LOG.error("${it.message}")
          Result(expected = chunk.size, successful = 0, failed = chunk.size, failures = chunk)
        }.getOrNull()
      }
    }
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }

}
