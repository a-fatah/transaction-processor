package it.zlick.converter

import it.zlick.converter.service.TransactionService
import org.apache.logging.log4j.LogManager
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class ConverterApplication {

  @Bean
  fun restTemplate(): RestTemplate = RestTemplate()

  @Bean
  fun clr(service: TransactionService) = CommandLineRunner {
    val summary = service.process(10, 10, "EUR");
    LOG.info(summary)
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }
}

fun main(args: Array<String>) {
  runApplication<ConverterApplication>(*args)
}
