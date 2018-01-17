package mx.finerio.api.services

import mx.finerio.api.exceptions.InstanceNotFoundException
import mx.finerio.api.domain.*
import mx.finerio.api.domain.repository.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MovementService {

  @Autowired
  MovementRepository movementRepository

  @Autowired
  AccountService accountService

  @Autowired
  CredentialPersistenceService credentialPersistenceService

  @Autowired
  ConceptService conceptService 

  Movement createMovement(Map params){
    def credential = credentialPersistenceService.findOne( params.request.credential_id )

    if ( !credential ) {
      throw new InstanceNotFoundException(
          'movement.createMovement.credential.null' )
    }
    def account = accountService.findById( params.request.account_id )
    if ( !account ) {
      throw new InstanceNotFoundException(
          'movement.createMovement.account.null' )
    }
    def transactionsSize = params.request.transactions.size
    for( int i=0; i < transactionsSize; i++ ){
       def date = new Date().parse("yyyy-MM-dd'T'HH:mm:ss",  params.request.transactions[i].made_on ) ?: new Date()
       def txAmount = params.request.transactions[ i ].amount
       def amount = new BigDecimal( txAmount ).abs().setScale( 2, BigDecimal.ROUND_HALF_UP )
       def type = txAmount < 0 ? Movement.Type.CHARGE : Movement.Type.DEPOSIT
       def params2 = [
            date: date,
            description: params.request.transactions[ i ].description,
            amount: amount,
            type: type,
            account: account
       ]
       def movement = movementRepository.findByDateAndDescriptionAndAmountAndTypeAndAccount( 
	params2.date, 
	params2.description, 
	params2.amount, 
	params2.type, 
	params2.account ) ?: new Movement()
       movement.dateCreated = new Date ()
       movement.lastUpdated = new Date ()
       movement.duplicated = params.request.transactions[i].duplicated ?: false
       movement.version = 0 
       movement.account = account
       movement.date = date
       movement.description = params.request.transactions[i].description
       movement.customDate = date
       movement.customDescription = params.request.transactions[i].description
       movement.amount = amount
       movement.balance = amount
       movement.type = type
       movementRepository.save(movement)
       createConcept( movement )
       movement
    }
  }

  private void createConcept( Movement movement ) throws Exception {

    def conceptData = [
      description: movement.description,
      amount: movement.amount,
      type: Concept.Type.DEFAULT ]
    conceptService.create( movement.id, conceptData )

  }



}
