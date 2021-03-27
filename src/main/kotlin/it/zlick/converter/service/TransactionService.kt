package it.zlick.converter.service

import it.zlick.converter.model.Transaction


interface TransactionService {
  fun process(n: Int, targetCurrency: String): Summary
}

data class Result(val expected: Int, val successful: Int, val failed: Int, val failures: List<Transaction>)
data class Summary(val expected: Int, val retrieved: Int, val conversion: Result, val processing: Result)
