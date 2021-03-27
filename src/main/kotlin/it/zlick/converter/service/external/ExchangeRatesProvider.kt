package it.zlick.converter.service.external

import java.time.LocalDate

interface ExchangeRatesProvider {
  /**
   * Returns a map of exchange rates from base currency to all available currencies in the system
   *
   * @return nullable map of exchange rates with key being a currency and value being the rate
   */
  fun getExchangeRates(base: String, date: LocalDate): Map<String, Float>?
}
