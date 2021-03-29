package it.zlick.converter

import com.fasterxml.jackson.databind.ObjectMapper
import it.zlick.converter.service.TransactionService
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@EnableConfigurationProperties(Config::class)
@SpringBootApplication
class ConverterApplication {

  @Bean
  fun restTemplate(objectMapper: ObjectMapper): RestTemplate {
    val restTemplate = RestTemplate(HttpComponentsClientHttpRequestFactory())
    restTemplate.messageConverters.add(0, MappingJackson2HttpMessageConverter(objectMapper))
    return restTemplate
  }

  @Bean
  fun clr(service: TransactionService, config: Config) = CommandLineRunner {
    runBlocking {
      service.process(config.transactions, config.targetCurrency);
    }
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }
}

fun main(args: Array<String>) {
  runApplication<ConverterApplication>(*args)

}
