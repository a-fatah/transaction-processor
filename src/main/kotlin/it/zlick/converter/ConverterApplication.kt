package it.zlick.converter

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import it.zlick.converter.service.TransactionService
import org.apache.logging.log4j.LogManager
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
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
  @Primary
  fun objectMapper(): ObjectMapper {
    val objectMapper = ObjectMapper().registerModule(JavaTimeModule()).registerModule(KotlinModule())
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    return objectMapper
  }

  @Bean
  fun clr(service: TransactionService) = CommandLineRunner {
    val summary = service.process(n=1, chunkSize = 10, targetCurrency = "EUR");
    LOG.info(summary)
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }
}

fun main(args: Array<String>) {
  runApplication<ConverterApplication>(*args)
}
