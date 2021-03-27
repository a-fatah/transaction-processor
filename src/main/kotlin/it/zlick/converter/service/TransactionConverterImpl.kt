package it.zlick.converter.service

import it.zlick.converter.exception.ExchangeRateNotFound
import it.zlick.converter.model.Transaction
import it.zlick.converter.service.external.ExchangeRatesProvider
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class TransactionConverterImpl(val ratesProvider: ExchangeRatesProvider): TransactionConverter {

  val calculator = FloatingPointCalculator()

  override fun convert(transaction: Transaction, targetCurrency: String): Transaction {
    val base = transaction.currency
    val date = transaction.createdAt.toLocalDate()

    val rates = ratesProvider.getExchangeRates(base, date)
      ?: throw ExchangeRateNotFound("API did not return exchange rates for base currency ${transaction.currency}")

    val exchangeRate = rates.get(targetCurrency)
      ?: throw ExchangeRateNotFound("The rates returned by API does not include exchange rate for ${targetCurrency}")

    LOG.debug("Conversion Rate for ${base}/${targetCurrency} found: ${exchangeRate}")

    val amount = calculator.convertAmount(transaction.amount, exchangeRate)

    return transaction.copy(amount = amount, currency = targetCurrency)
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }

}
