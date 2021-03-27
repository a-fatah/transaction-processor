package it.zlick.converter.service

import it.zlick.converter.model.Transaction

interface TransactionConverter {
  /**
   * Converts a transaction to target currency
   *
   * @param transaction to be converted
   * @param target currency code for the target currency
   */
  fun convert(transaction: Transaction, target: String): Transaction
}

