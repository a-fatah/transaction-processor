package it.zlick.converter.service.external.impl

import it.zlick.converter.exception.ProcessingError
import it.zlick.converter.model.Transaction
import it.zlick.converter.service.external.ProcessResult
import it.zlick.converter.service.external.TransactionProcessor
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class TransactionProcessorImpl(
  @Value("\${api.process.url}") private val apiUrl: String,
  private val restTemplate: RestTemplate
  ): TransactionProcessor {

  @Value("\${process.chunk-size}")
  private val MAX_CHUNK_SIZE = 10

  override fun process(transactions: List<Transaction>): ProcessResult {
    if(transactions.isEmpty()) {
      throw ProcessingError("Received empty list of transactions for processing!")
    }

    if(transactions.size > MAX_CHUNK_SIZE) {
      throw ProcessingError("# of transactions exceed MAX_CHUNK_SIZE: ${MAX_CHUNK_SIZE}")
    }

    try {
      LOG.info("Sending ${transactions.size} transactions for processing...")
      LOG.debug("POST ${apiUrl}")
      val res = restTemplate.postForEntity(apiUrl, transactions, ProcessResult::class.java)
      return res.body!!
    } catch(e: RestClientException) {
      throw ProcessingError("Error while sending transactions for processing: ${e.message}")
    }
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }
}
