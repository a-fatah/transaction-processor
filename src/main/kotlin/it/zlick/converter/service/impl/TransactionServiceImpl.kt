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

@Service
class TransactionServiceImpl(
  private val provider: TransactionProvider,
  private val converter: TransactionConverter,
  private val processor: TransactionProcessor
  ): TransactionService {

  override fun process(n: Int, chunkSize: Int, targetCurrency: String): Summary {
    val transactions = fetchTransactions(n)
    val converted = convertTransactions(transactions, targetCurrency)
    val processed = processTransactions(converted, chunkSize)

    return Summary(
      expected = n,
      retrieved = transactions.size,
      conversion = Result(
        expected = transactions.size,
        successful = converted.size,
        failed = transactions.size - converted.size,
        failures = transactions.filter { it.checksum !in converted.map { it.checksum } }
      ),
      processing = Result(
        expected = converted.size,
        successful = processed.successful,
        failed = processed.failed,
        failures = emptyList()
      )
    )
  }

  private fun fetchTransactions(n: Int): List<Transaction> {
    val fetchCalls = (1..n).map {
      runCatching {
        provider.getTransaction()
      }
    }
    fetchCalls.filter { it.isFailure }.forEach {
      LOG.error(it.exceptionOrNull()?.message)
    }
    return fetchCalls.filter { it.isSuccess }.map { it.getOrNull() }.filterNotNull()
  }

  private fun convertTransactions(transactions: List<Transaction>, targetCurrency: String): List<Transaction> {
    // TODO cache exchange rates to avoid
    val conversionResults = transactions.map {
      runCatching {
        converter.convert(it, targetCurrency)
      }
    }
    conversionResults.filter { it.isFailure }.forEach {
      LOG.error("${it.exceptionOrNull()?.message}")
    }
    return conversionResults.filter { it.isSuccess }.map { it.getOrNull() }.filterNotNull()
  }

  private fun processTransactions(transactions: List<Transaction>, chunkSize: Int): ProcessResult {
    val processingResults = transactions.chunked(chunkSize).map {
      runCatching {
        processor.process(it)
      }
    }
    processingResults.filter { it.isFailure }.forEach {
      LOG.error("Error while processing transactions ${it.exceptionOrNull()}")
    }
    val total = transactions.size
    val successful = processingResults.filter { it.isSuccess }.map { it.getOrNull() }.filterNotNull().sumBy { it.passed }
    val failed = processingResults.filter { it.isFailure }.count() * chunkSize
    return ProcessResult(total=total, successful=successful, failed=failed)
  }

  data class ProcessResult(val total: Int, val successful: Int, val failed: Int)

  companion object {
    private val LOG = LogManager.getLogger()
  }

}
