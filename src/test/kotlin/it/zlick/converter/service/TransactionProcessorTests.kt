package it.zlick.converter.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import it.zlick.converter.model.Transaction
import it.zlick.converter.service.external.ProcessResult
import it.zlick.converter.service.external.impl.TransactionProcessorImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class TransactionProcessorTests {

  val API_URL = "https://api.zlick.it/process-transactions"

  @Test
  fun `given a transaction it calls process-transaction endpoint with POST method`() {
    // arrange
    val restTemplate = mockk<RestTemplate>()
    val dummyResponse = ResponseEntity.ok(ProcessResult(success = true, passed = 0, failed = 0))
    val urlSlot = slot<String>()
    every {
      restTemplate.postForEntity(
        capture(urlSlot),
        any(),
        ProcessResult::class.java
      )
    } returns dummyResponse

    val exchangeService = TransactionProcessorImpl(API_URL, restTemplate);

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
