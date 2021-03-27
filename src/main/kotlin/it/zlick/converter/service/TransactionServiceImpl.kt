package it.zlick.converter.service

import it.zlick.converter.model.Transaction
import it.zlick.converter.service.external.ProcessResult
import it.zlick.converter.service.external.TransactionProcessor
import it.zlick.converter.service.external.TransactionProvider
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class TransactionServiceImpl(
  val provider: TransactionProvider,
  val converter: TransactionConverter,
  val processor: TransactionProcessor
  ): TransactionService {

  override fun process(n: Int, targetCurrency: String): Summary {
    val transactions = fetchTransactions(n)
    val converted = convertTransactions(transactions, targetCurrency)
    val processed = processTransactions(converted)

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
        successful = processed.sumBy { it.passed },
        failed = processed.sumBy { it.failed },
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

    val failedFetches = fetchCalls.filter { it.isFailure }
    failedFetches.forEach {
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
    val failedConversions = conversionResults.filter { it.isFailure }
    failedConversions.forEach {
      LOG.error("${it.exceptionOrNull()?.message}")
    }
    return conversionResults.filter { it.isSuccess }.map { it.getOrNull() }.filterNotNull()
  }

  private fun processTransactions(transactions: List<Transaction>): List<ProcessResult> {
    val processingResults = transactions.chunked(10).map {
      runCatching {
        processor.process(it)
      }
    }

    val failedProcesses = processingResults.filter { it.isFailure }

    failedProcesses.forEach {
      LOG.error("Error while processing transactions ${it.exceptionOrNull()}")
    }

    return processingResults.filter { it.isSuccess }.map { it.getOrNull() }.filterNotNull()
  }


  companion object {
    private val LOG = LogManager.getLogger()
  }

}
