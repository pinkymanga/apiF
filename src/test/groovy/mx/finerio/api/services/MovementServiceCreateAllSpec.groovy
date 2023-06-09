package mx.finerio.api.services

import mx.finerio.api.domain.Account
import mx.finerio.api.domain.Category
import mx.finerio.api.domain.Movement
import mx.finerio.api.domain.repository.CategoryRepository
import mx.finerio.api.domain.repository.MovementRepository
import mx.finerio.api.dtos.Transaction
import mx.finerio.api.dtos.TransactionData
import mx.finerio.api.exceptions.BadImplementationException

import spock.lang.Specification

class MovementServiceCreateAllSpec extends Specification {

  def service = new MovementService()

  def accountService = Mock( AccountService )
  def categorizerService = Mock( CategorizerService )
  def cleanerService = Mock( CleanerService )
  def categoryRepository = Mock( CategoryRepository )
  def movementRepository = Mock( MovementRepository )

  def setup() {

    service.accountService = accountService
    service.categorizerService = categorizerService
    service.cleanerService = cleanerService
    service.categoryRepository = categoryRepository
    service.movementRepository = movementRepository

  }

  def "invoking method successfully"() {

    when:
      def result = service.createAll( transactionData )
    then:
      1 * accountService.findByIdAndCredentialId( _ as String, _ as String ) >> new Account(id: 'idAccount')
      3 * cleanerService.clean( _ as String, _ as Boolean ) >> 'hello'
      3 * categorizerService.search( _ as String, _ as Boolean ) >>
          [ categoryId: 'categoryId' ]
      3 * categoryRepository.findOne( _ as String ) >> new Category()
      3 * movementRepository.save( _ as Movement ) >> new Movement()
      result instanceof List
      result.size() == 3
      result[ 2 ] instanceof Movement
    where:
      transactionData = getTransactionData()

  }

  def "previous movements found"() {

    when:
      def result = service.createAll( transactionData )
    then:
    1 * accountService.findByIdAndCredentialId( _ as String, _ as String ) >> new Account(id: 'idAccount')
      3 * movementRepository.save( _ as Movement ) >> new Movement()
      result instanceof List
      result.size() == 3
    where:
      transactionData = getTransactionData()

  }

  def "movements with empty description"() {

    given:
      transactionData.transactions.each { it.description = '' }
    when:
      def result = service.createAll( transactionData )
    then:
    1 * accountService.findByIdAndCredentialId( _ as String, _ as String ) >> new Account(id: 'idAccount')
    0 * movementRepository.findFirstByDateAndDescriptionAndAmountAndTypeAndAccountAndScraperDuplicatedIdIsNullOrderByDateCreatedDesc(
        _ as Date, _ as String, _ as BigDecimal, _ as Movement.Type,
        _ as Account ) >> new Movement()
      0 * movementRepository.save( _ as Movement ) >> new Movement()
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
      e.message == 'movementService.createAll.transactionData.null'
    where:
      transactionData = null

  }

  private TransactionData getTransactionData() throws Exception {

    new TransactionData(
      credential_id: 'cred_id',
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

  private Transaction getTransaction() throws Exception {

    new Transaction(
      made_on: '2100-12-31T00:00:00',
      description: 'description',
      amount: 1.00
    )

  }

  private Boolean validateResult( List movements ) throws Exception {

    movements.each { movement ->
      assert movement.account != null
      assert movement.date != null
      assert movement.customDate != null
      assert movement.date == movement.customDate
      assert movement.description != null
      assert movement.customDescription != null
      assert movement.description == movement.customDescription
      assert movement.amount != null
      assert movement.balance != null
      assert movement.amount == movement.balance
      assert movement.type != null
      assert movement.dateCreated != null
      assert movement.lastUpdated != null
      assert movement.version == 0

    }

  }

}
