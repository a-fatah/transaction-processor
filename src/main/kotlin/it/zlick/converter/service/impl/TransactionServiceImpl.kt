package it.zlick.converter.service.impl

import it.zlick.converter.service.TransactionConverter
import it.zlick.converter.service.TransactionService
import it.zlick.converter.service.external.ProcessResult
import it.zlick.converter.service.external.TransactionProcessor
import it.zlick.converter.service.external.TransactionProvider
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TransactionServiceImpl(
  private val provider: TransactionProvider,
  private val converter: TransactionConverter,
  private val processor: TransactionProcessor
  ): TransactionService {

  @Value("\${processor.chunk-size}")
  private val chunkSize = 10

  override fun process(n: Int, targetCurrency: String) {
    val chunks = n / chunkSize
    val results = mutableListOf<ProcessResult>()

    runBlocking {
      (1..chunks).forEach {
        launch {
          (1..chunkSize).asFlow().map {
            provider.getTransaction()
          }.map {
            converter.convert(it, targetCurrency)
          }.toList().also {
            val result = processor.process(it)
            results.add(result)
          }
        }

      }
    }

    LOG.info(results)
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }

}
