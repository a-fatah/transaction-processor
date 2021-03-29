package it.zlick.converter.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class CurrencyConverterTests {

  @ParameterizedTest
  @MethodSource("testInput")
  fun `given an amount and exchange rate it returns converted amount with 4 decimal places`(input: Float, rate: Float, expected: Float) {
    val converter = CurrencyConverter()
    val result = converter.convert(input, rate)
    assertThat(result).isEqualTo(expected)
  }

  companion object {
    @JvmStatic
    fun testInput(): Stream<Arguments> = Stream.of(
      arguments(1.12535879f, 1.0f, 1.1254f),
      arguments(2.25643789f, 2.5f, 5.6411f)
    )
  }
}
