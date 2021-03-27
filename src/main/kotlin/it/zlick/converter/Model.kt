package it.zlick.converter.model

import java.time.LocalDateTime

data class Transaction(
  val createdAt: LocalDateTime=LocalDateTime.now(),
  val currency: String,
  val amount: Float,
  val convertedAmount: Float = 0.0f,
  val checksum: String
)
