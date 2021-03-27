package it.zlick.converter.service.adapter

import it.zlick.converter.exception.ExchangeRateAPIError
import it.zlick.converter.service.external.ExchangeRatesProvider
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class ExchangeRatesProviderImpl(@Value("\${api.exchange.url}") val apiUrl: String, val restTemplate: RestTemplate):
  ExchangeRatesProvider {

  override fun getExchangeRates(base: String, date: LocalDate): Map<String, Float> {
    val params = mapOf("date" to date.format(DateTimeFormatter.ISO_DATE), "base" to base)
    LOG.debug("Sending GET to ${apiUrl} with params ${params}...")

    try {
      val response = restTemplate.getForEntity(apiUrl, APIResponse::class.java, params)
      return response.body!!.rates
    } catch(e: RestClientException) {
      throw ExchangeRateAPIError("Error while getting exchange rate. ${e.message}")
    }
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }

}

data class APIResponse(val rates: Map<String, Float>)
