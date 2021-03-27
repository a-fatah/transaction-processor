package it.zlick.converter.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import it.zlick.converter.exception.ExchangeRateNotFound
import it.zlick.converter.exception.FetchException
import it.zlick.converter.exception.ProcessingError
import it.zlick.converter.service.external.TransactionProcessor
import it.zlick.converter.service.external.TransactionProvider
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

class TransactionServiceTests {

  val provider = mockk<TransactionProvider>()
  val converter = mockk<TransactionConverter>()
  val processor = mockk<TransactionProcessor>()

  val transactionService = TransactionServiceImpl(provider, converter, processor)

  @BeforeEach
  fun init() {
    clearMocks(provider, converter, processor)
  }

  @Test
  fun `given transactions fetched then it should return number of fetched transactions`() {
    // arrange
    every { provider.getTransaction() } returns mockk() {
      every { checksum } returns "test"
    }
    // act
    val result = transactionService.process(10, 10, "USD")

    // assert
    assertThat(result.retrieved).isEqualTo(10)
  }

  @Test
  fun `given transactions could not be fetched then it returns 0 in result summary`() {
    // arrange
    every { provider.getTransaction() } throws FetchException("Could not fetch transaction")
    // act
    val result = transactionService.process(10, 10, "USD")
    // assert
    assertThat(result.retrieved).isEqualTo(0)
  }

  @Test
  fun `given all transacitons fail to convert then it returns number of failed conversions`() {
    // arrange
    every { provider.getTransaction() } returns mockk() {
      every { checksum } returns "test"
    }
    every { converter.convert(any(), any()) } throws ExchangeRateNotFound("Exchange rate not found")
    // act
    val result = transactionService.process(10, 10, "USD")

    // assert
    assertThat(result.conversion.failed).isEqualTo(10)
  }

  @Test
  fun `given transacitons failed to process then it returns number of failed transactions`() {
    // arrange
    every { provider.getTransaction() } returns mockk() {
      every { checksum } returns "test"
    }
    every { converter.convert(any(), any()) } returns mockk() {
      every { checksum } returns "test"
    }
    every { processor.process(any()) } throws ProcessingError("Some error occured")
    // act
    val result = transactionService.process(10, 5, "USD")

    // assert
    assertThat(result.processing.failed).isEqualTo(10)
  }

}
