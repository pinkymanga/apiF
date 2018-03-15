package mx.finerio.api.services

import mx.finerio.api.domain.Account
import mx.finerio.api.domain.Transaction
import mx.finerio.api.domain.repository.TransactionRepository
import mx.finerio.api.dtos.Transaction as TransactionCreateDto
import mx.finerio.api.dtos.TransactionData
import mx.finerio.api.exceptions.BadImplementationException

import spock.lang.Specification

class TransactionServiceCreateAllSpec extends Specification {

  def service = new TransactionService()

  def accountService = Mock( AccountService )
  def sha1Service = Mock( Sha1Service )
  def transactionRepository = Mock( TransactionRepository )

  def setup() {

    service.accountService = accountService
    service.sha1Service = sha1Service
    service.transactionRepository = transactionRepository

  }

  def "invoking method successfully"() {

    when:
      def result = service.createAll( transactionData )
    then:
      1 * accountService.findById( _ as String ) >> new Account()
      3 * sha1Service.encrypt( _ as String ) >> 'hash'.bytes
      3 * transactionRepository.
          findByAccountAndHashAndDateDeletedIsNull(
          _ as Account, _ as byte[] ) >> null
      3 * transactionRepository.save( _ as Transaction ) >> new Transaction()
      result instanceof List
      result.size() == 3
      result[ 2 ] instanceof Transaction
    where:
      transactionData = getTransactionData()

  }

  def "previous transactions found"() {

    when:
      def result = service.createAll( transactionData )
    then:
      1 * accountService.findById( _ as String ) >> new Account()
      3 * sha1Service.encrypt( _ as String ) >> 'hash'.bytes
      3 * transactionRepository.
          findByAccountAndHashAndDateDeletedIsNull(
          _ as Account, _ as byte[] ) >> new Transaction()
      0 * transactionRepository.save( _ as Transaction )
      result instanceof List
      result.size() == 0
    where:
      transactionData = getTransactionData()

  }

  def "parameter 'transactionData' is null"() {

    when:
      service.createAll( transactionData )
    then:
      BadImplementationException e = thrown()
      e.message == 'transactionService.createAll.transactionData.null'
    where:
      transactionData = null

  }

  private TransactionData getTransactionData() throws Exception {

    new TransactionData(
      account_id: 'account_id',
      transactions: getTransactions()
    )

  }

  private List getTransactions() throws Exception {

    [
      getTransaction(),
      getTransaction(),
      getTransaction()
    ]

  }

  private TransactionCreateDto getTransaction() throws Exception {

    new TransactionCreateDto(
      made_on: '2100-12-31T12:34:56',
      description: 'description',
      amount: 1.00
    )

  }

  private Boolean validateResult( List transactions ) throws Exception {

    transactions.each { transaction ->
      assert transaction.account != null
      assert transaction.date != null
      assert transaction.customDate != null
      assert transaction.date == transaction.customDate
      assert transaction.description != null
      assert transaction.customDescription != null
      assert transaction.description == transaction.customDescription
      assert transaction.amount != null
      assert transaction.balance != null
      assert transaction.amount == transaction.balance
      assert transaction.type != null
      assert transaction.dateCreated != null
      assert transaction.lastUpdated != null
      assert transaction.version == 0

    }

  }

}
