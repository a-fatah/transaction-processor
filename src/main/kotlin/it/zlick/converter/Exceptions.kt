package it.zlick.converter.exception


class ExchangeRateNotFound(message: String?): Exception(message)
class TransactionNotProcessed(message: String?): Exception(message)
class TransactionNotFound(message: String?): Exception(message)
class ProcessingError(message: String?): Exception(message)
