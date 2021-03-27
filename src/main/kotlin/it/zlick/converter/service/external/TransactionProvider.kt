package it.zlick.converter.service.external

import it.zlick.converter.model.Transaction

interface TransactionProvider {
  fun getTransaction(): Transaction?
}
