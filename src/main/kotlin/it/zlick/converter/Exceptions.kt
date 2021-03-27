package it.zlick.converter.exception


class ExchangeRateNotFound(message: String?): Exception(message)
class ExchangeRateAPIError(message: String?): Exception(message)
class FetchException(message: String?): Exception(message)
class ProcessingError(message: String?): Exception(message)
