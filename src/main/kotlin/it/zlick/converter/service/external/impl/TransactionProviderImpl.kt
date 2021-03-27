package it.zlick.converter.service.external.impl

import it.zlick.converter.model.Transaction
import it.zlick.converter.service.external.TransactionProvider
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class TransactionProviderImpl(@Value("\${api.transaction.url}") val apiUrl: String, val restTemplate: RestTemplate):
  TransactionProvider {

  override fun getTransaction(): Transaction? {
    try {
      val response = restTemplate.getForEntity(apiUrl, Transaction::class.java)
      return response.body
    } catch(e: RestClientException) {
      LOG.error("Error while calling transaction API: ${e.message}")
      return null
    }
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }

}
