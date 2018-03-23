package mx.finerio.api.services

import mx.finerio.api.domain.Callback
import mx.finerio.api.domain.Credential
import mx.finerio.api.dtos.FailureCallbackDto
import mx.finerio.api.dtos.SuccessCallbackDto
import mx.finerio.api.dtos.TransactionDto
import mx.finerio.api.exceptions.BadImplementationException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ScraperCallbackService {

  @Autowired
  CallbackService callbackService

  @Autowired
  CredentialService credentialService

  @Autowired
  MovementService movementService

  @Autowired
  ScraperWebSocketService scraperWebSocketService

  @Autowired
  TransactionService transactionService

  void processTransactions( TransactionDto transactionDto ) throws Exception {

    validateProcessTransactionsInput( transactionDto )
    def movements = movementService.createAll( transactionDto.data )
    def transactions = transactionService.createAll( transactionDto.data )
    def credential = credentialService.findAndValidate(
        transactionDto?.data?.credential_id as String )
    callbackService.sendToClient( credential?.customer?.client,
        Callback.Nature.TRANSACTIONS, [ credentialId: credential.id,
        accountId: transactionDto.data.account_id ] )
    movements.each { movementService.createConcept( it ) }

    if ( credential?.customer?.client?.categorizeTransactions ) {

      transactions.each { transactionService.categorize( it ) }
      callbackService.sendToClient( credential?.customer?.client,
          Callback.Nature.NOTIFY, [ credentialId: credential.id,
          accountId: transactionDto.data.account_id,
          stage: 'categorize_transactions' ] )

    }

  }

  void processSuccess( SuccessCallbackDto successCallbackDto )
      throws Exception {

    if ( !successCallbackDto ) {
      throw new BadImplementationException(
          'scraperCallbackService.processSuccess.successCallbackDto.null' )
    }

    def credential = credentialService.updateStatus(
        successCallbackDto?.data?.credential_id, Credential.Status.ACTIVE )
    callbackService.sendToClient( credential?.customer?.client,
        Callback.Nature.SUCCESS, [ credentialId: credential.id ] )
    closeWebSocketSession( credential )

  }

  void processFailure( FailureCallbackDto failureCallbackDto ) throws Exception {

    if ( !failureCallbackDto ) {
      throw new BadImplementationException(
          'scraperCallbackService.processFailure.failureCallbackDto.null' )
    }

    def credential = credentialService.setFailure(
        failureCallbackDto?.data?.credential_id,
        failureCallbackDto?.data?.error_message )
    callbackService.sendToClient( credential?.customer?.client,
        Callback.Nature.FAILURE, [ credentialId: credential.id,
        message: credential.errorCode  ] )
    closeWebSocketSession( credential )

  }

  private void validateProcessTransactionsInput(
      TransactionDto transactionDto ) throws Exception {

    if ( !transactionDto ) {
      throw new BadImplementationException(
          'scraperCallbackService.processTransactions.transactionDto.null' )
    }

  }

  private void closeWebSocketSession( Credential credential )
      throws Exception {

    if ( credential.institution.code != 'BBVA' ) {
      return
    }

    scraperWebSocketService.closeSession( credential.id )

  }

}