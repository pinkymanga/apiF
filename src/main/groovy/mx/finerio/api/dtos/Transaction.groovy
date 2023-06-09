package mx.finerio.api.dtos

import groovy.transform.ToString

@ToString(includePackage = false, includeNames = true)
class Transaction {

  String made_on
  String description
  BigDecimal amount
  TransactionExtraData extra_data

}
