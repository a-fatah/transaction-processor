package it.zlick.converter

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "processor")
@ConstructorBinding
data class Config(
  val transactions: Int,
  val targetCurrency: String,
  val chunkSize: Int,
  val concurrent: ConcurrencyConfig
)

data class ConcurrencyConfig(
  val fetch: Int,
  val convert: Int,
  val process: Int
)
