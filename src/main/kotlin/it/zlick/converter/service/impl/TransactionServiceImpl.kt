package it.zlick.converter.service.impl

import it.zlick.converter.model.Transaction
import it.zlick.converter.service.TransactionConverter
import it.zlick.converter.service.TransactionService
import it.zlick.converter.service.external.TransactionProcessor
import it.zlick.converter.service.external.TransactionProvider
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TransactionServiceImpl(
  private val provider: TransactionProvider,
  private val converter: TransactionConverter,
  private val processor: TransactionProcessor
) : TransactionService {

  @Value("\${processor.chunk-size}")
  private val chunkSize = 10

  @Value("\${processor.concurrent.fetch}")
  private val fetchThreads = 5

  @Value("\${processor.concurrent.convert}")
  private val convertThreads = 5

  @Value("\${processor.concurrent.process}")
  private val processThreads = 5

  override fun process(n: Int, targetCurrency: String) = runBlocking {
    var fetched = 0
    var converted = 0
    var processed = 0

    fun logStatistics() {
      LOG.info("Fetched: ${fetched}\tConverted: ${converted}\tProcessed: ${processed}")
    }

    val chunks = n / chunkSize

    val fetchers = newFixedThreadPoolContext(fetchThreads, "fetcher")
    val converters = newFixedThreadPoolContext(convertThreads, "converter")
    val processors = newFixedThreadPoolContext(processThreads, "processor")

    val process = Channel<List<Transaction>>()

    repeat(chunks) {
      launch {
        val transactions = mutableListOf<Transaction>()
        (1..chunkSize).asFlow()
          .map {
            LOG.debug("Fetching transaction...")
            runCatching {
              provider.getTransaction()
            }.onSuccess {
              fetched++
              logStatistics()
            }
          }.flowOn(fetchers).buffer()
          .map {
            it.onSuccess { // convert only if transaction was fetched successfully
              LOG.debug("Converting transaction...")
              runCatching {
                converter.convert(it, targetCurrency)
              }.onSuccess {
                converted++
                logStatistics()
              }
            }
          }.flowOn(converters).buffer()
          .collect {
            it.onSuccess {
              transactions.add(it!!)
            }.onFailure {
              LOG.error(it.message)
            }
          }
        process.send(transactions)
      }
    }

    process.consumeEach { list ->
      launch(processors) {
        runCatching {
          processor.process(list)
        }.onSuccess {
          processed += list.size
          logStatistics()
        }.onFailure {
          LOG.error("Failed to process transactions: ${it.message}")
        }
      }
    }

  }

  companion object {
    private val LOG = LogManager.getLogger()
  }

}
