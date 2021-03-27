package it.zlick.converter.service

import io.mockk.*
import it.zlick.converter.exception.ProcessingError
import it.zlick.converter.model.Transaction
import it.zlick.converter.service.external.ProcessResult
import it.zlick.converter.service.external.impl.TransactionProcessorImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import kotlin.random.Random

class TransactionProcessorTests {
  val API_URL = "https://api.zlick.it/process-transactions"
  val restTemplate = mockk<RestTemplate>()
  val exchangeService = TransactionProcessorImpl(API_URL, restTemplate);

  @BeforeEach
  fun init() {
    clearMocks(restTemplate)
  }

  @Test
  fun `given empty list of transactions it throws an exception`() {
    // arrange
    val transactions = emptyList<Transaction>()

    // act
    val thrown = assertThrows<ProcessingError> {
      exchangeService.process(transactions)
    }

    // assert
    assertThat(thrown.message).isEqualTo("Received empty list of transactions for processing!")
  }

  @Test
  fun `given a list of transactions having more than maximum allowed transactions then throws exception`() {
    // arrange
    val transactions = List(20, {
      Transaction(currency = "USD", amount = Random.nextFloat() * 100, checksum = "test")
    })

    // act
    val thrown = assertThrows<ProcessingError> {
      exchangeService.process(transactions)
    }

    // assert
    assertThat(thrown.message).startsWith("# of transactions exceed MAX_CHUNK_SIZE")
  }

  @Test
  fun `given a transaction it calls process-transaction endpoint with POST method`() {
    // arrange
    val dummyResponse = ResponseEntity.ok(ProcessResult(success = true, passed = 0, failed = 0))
    val urlSlot = slot<String>()
    every {
      restTemplate.postForEntity(
        capture(urlSlot),
        any(),
        ProcessResult::class.java
      )
    } returns dummyResponse

    val transactions = listOf(Transaction(currency = "USD", amount = 10.0f, checksum = "test"))

    // act
    exchangeService.process(transactions)

    // assert
    assertThat(urlSlot.captured).isEqualTo(API_URL)

    verify(exactly = 1) {
      restTemplate.postForEntity(
        API_URL,
        transactions,
        ProcessResult::class.java
      )
    }
  }
}
