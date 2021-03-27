package it.zlick.converter.service

import java.math.BigDecimal
import java.math.RoundingMode

class FloatingPointCalculator {
  fun convertAmount(amount: Float, rate: Float): Float {
    val amount = BigDecimal.valueOf(amount.toDouble())
    val rate = BigDecimal.valueOf(rate.toDouble())
    return amount.multiply(rate).setScale(4, RoundingMode.HALF_UP).toFloat()
  }
}
