package it.zlick.converter.service

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.zlick.converter.service.adapter.APIResponse
import it.zlick.converter.service.adapter.ExchangeRatesProviderImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GetExchangeRatesTests {

  val restTemplate = mockk<RestTemplate>()
  val API_URL = "https://api.exchangeratesapi.io/{date}?base={base}"

  @BeforeEach
  fun init() {
    clearMocks(restTemplate)
  }

  @Test
  fun `given base currency and date it calls api with correct parameters`() {
    // arrange

    every {
      restTemplate.getForEntity(
        any(),
        APIResponse::class.java,
        any())
    } returns ResponseEntity.ok(APIResponse(emptyMap()))

    val exchangeService = ExchangeRatesProviderImpl(API_URL, restTemplate);
    val baseCurrency = "USD"
    val date = LocalDate.now()

    // act
    exchangeService.getExchangeRates(baseCurrency, date);

    // assert
    val expectedDateParam = date.format(DateTimeFormatter.ISO_DATE)

    verify(exactly = 1) {
      restTemplate.getForEntity(
        "https://api.exchangeratesapi.io/{date}?base={base}",
        APIResponse::class.java,
        mapOf("date" to expectedDateParam, "base" to baseCurrency)
      )
    }
  }

  @Test
  fun `given a currency and date when API request is successful then it returns map of exchange rates`() {
    // arrange
    val dummyRates = mapOf(
      "USD" to 1.23f,
      "EUR" to 1.50f
    )

    every {
      restTemplate.getForEntity(
        any(),
        APIResponse::class.java,
        any())
    } returns ResponseEntity.ok(APIResponse(dummyRates))

    val exchangeService = ExchangeRatesProviderImpl(API_URL, restTemplate);

    // act
    val rates = exchangeService.getExchangeRates("USD", LocalDate.now());

    // assert
    assertThat(rates).isEqualTo(dummyRates)
  }

}
