package it.zlick.converter.service

import it.zlick.converter.exception.ExchangeRateNotFound
import it.zlick.converter.model.Transaction
import it.zlick.converter.service.external.ExchangeRatesProvider
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service

@Service
class TransactionConverterImpl(val ratesProvider: ExchangeRatesProvider): TransactionConverter {

  val calculator = FloatingPointCalculator()

  override fun convert(transaction: Transaction, targetCurrency: String): Transaction {
    val base = transaction.currency
    val date = transaction.createdAt.toLocalDate()

    val rates = ratesProvider.getExchangeRates(base, date)

    val exchangeRate = rates.get(targetCurrency)
      ?: throw ExchangeRateNotFound("Exchange rate for ${base}/${targetCurrency} not found")

    LOG.debug("Exchange Rate for ${base}/${targetCurrency} found: ${exchangeRate}")

    val amount = calculator.convertAmount(transaction.amount, exchangeRate)

    return transaction.copy(amount = amount, currency = targetCurrency)
  }

  companion object {
    private val LOG = LogManager.getLogger()
  }

}
