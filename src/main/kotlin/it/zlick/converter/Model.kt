package it.zlick.converter.model

import java.time.LocalDateTime
import javax.money.CurrencyUnit
import javax.money.NumberValue

data class Transaction(
  val createdAt: LocalDateTime=LocalDateTime.now(),
  val currency: String,
  val amount: Float,
  val checksum: String
)
