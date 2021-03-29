package it.zlick.converter

import com.fasterxml.jackson.databind.ObjectMapper
import it.zlick.converter.service.TransactionService
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class ConverterApplication {

  @Bean
  fun restTemplate(objectMapper: ObjectMapper): RestTemplate {
    val restTemplate = RestTemplate(HttpComponentsClientHttpRequestFactory())
    restTemplate.messageConverters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
    return restTemplate
  }

  @Bean
  fun clr(service: TransactionService, @Value("\${processor.target}") target: Int) = CommandLineRunner {
    val summary = service.process(target, targetCurrency = "EUR");
    LOG.info(summary)
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }
}

fun main(args: Array<String>) {
  runApplication<ConverterApplication>(*args)

}
