package it.zlick.converter.service

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
    val transactions = (1..n).map { provider.getTransaction() }.filterNotNull()

    // TODO cache exchange rates to avoid
    val converted = transactions.map {
      runCatching {
        converter.convert(it, targetCurrency)
      }.recover {
        LOG.error("Error while converting transaction ${it.message}")
        null
      }.getOrNull()
    }.filterNotNull()

    val processed = converted.chunked(10).map {
      runCatching {
        processor.process(it)
      }.recover {
        LOG.error("Error while processing transactions: ${it.message}")
        null
      }.getOrNull()
    }.filterNotNull()

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

  companion object {
    private val LOG = LogManager.getLogger()
  }

}
