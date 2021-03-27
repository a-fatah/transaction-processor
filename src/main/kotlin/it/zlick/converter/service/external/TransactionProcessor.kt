package it.zlick.converter.service.external

import it.zlick.converter.model.Transaction

interface TransactionProcessor {
  fun process(transactions: List<Transaction>): ProcessResult?
}

data class ProcessResult(val success: Boolean, val passed: Int, val failed: Int)
