package mx.finerio.api.controllers

import javax.servlet.http.HttpServletRequest

import mx.finerio.api.domain.Callback
import mx.finerio.api.domain.Credential
import mx.finerio.api.dtos.AccountDto
import mx.finerio.api.dtos.FailureCallbackDto
import mx.finerio.api.dtos.TransactionDto
import mx.finerio.api.dtos.NotifyCallbackDto
import mx.finerio.api.dtos.SuccessCallbackDto
import mx.finerio.api.services.AccountDetailsService
import mx.finerio.api.services.AccountService
import mx.finerio.api.services.AzureQueueService
import mx.finerio.api.services.CallbackService
import mx.finerio.api.services.CredentialService
import mx.finerio.api.services.ScraperCallbackService
import mx.finerio.api.domain.TransactionMessageType

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import mx.finerio.api.services.CredentialStateService


@RestController
class ScraperCallbackController {

  @Autowired
  AccountDetailsService accountDetailsService

  @Autowired
  AccountService accountService

  @Autowired
  CallbackService callbackService

  @Autowired
  CredentialService credentialService

  @Autowired
  ScraperCallbackService scraperCallbackService
  
  @Autowired
  AzureQueueService azureQueueService

  @Autowired
  CredentialStateService credentialStateService

  @PostMapping( '/callbacks/accounts' )
  ResponseEntity accounts( @RequestBody AccountDto accountDto ) {

    def account = accountService.create( accountDto.data )
    def credential = credentialService.findAndValidate(
        accountDto?.data?.credential_id as String )
    def accountDetails = accountDetailsService.findAllByAccount( account.id )
    
    def data = [ credentialId: credential.id,
        accountId: account.id,
        account: accountService.getFields( account ),
        accountDetails: accountDetails ]

    credentialStateService.addState( credential.id, data )
    callbackService.sendToClient( credential?.customer?.client,
        Callback.Nature.ACCOUNTS, data )

    ResponseEntity.ok( [ id: account.id ] )

  }

  @PostMapping( '/callbacks/transactions' )
  ResponseEntity transactions( @RequestBody TransactionDto transactionDto ) {
    azureQueueService.queueTransactions( transactionDto, TransactionMessageType.CONTENT )
    ResponseEntity.ok().build()

  }

  @PostMapping( '/callbacks/success' )
  ResponseEntity success(
      @RequestBody SuccessCallbackDto successCallbackDto ) {
     TransactionDto transactionDto = 
      TransactionDto.getInstanceFromCredentialId( successCallbackDto.data?.credential_id )
      azureQueueService.queueTransactions( transactionDto, TransactionMessageType.END )
    ResponseEntity.ok().build()

  }

  @PostMapping( '/callbacks/failure' )
  ResponseEntity failure(
      @RequestBody FailureCallbackDto failureCallbackDto ) {
    scraperCallbackService.processFailure( failureCallbackDto )
    ResponseEntity.ok().build()

  }

  @PostMapping( '/callbacks/notify' )
  ResponseEntity notify( @RequestBody NotifyCallbackDto request ) {
    queueStartNotify(request)
    def credential = credentialService.findAndValidate(
        request?.data?.credential_id as String )
    def data = [ credentialId: credential.id,
        stage: request?.data?.stage  ]
    credentialStateService.addState( credential.id, data )
    callbackService.sendToClient( credential?.customer?.client,
        Callback.Nature.NOTIFY, data )

  }

  private queueStartNotify( NotifyCallbackDto request ){
    if( request && request?.data?.credential_id 
        && request?.data?.stage?.equals("fetch_transactions") ){

      TransactionDto transactionDto = 
      TransactionDto.getInstanceFromCredentialId( request.data?.credential_id )
      azureQueueService.queueTransactions( transactionDto, TransactionMessageType.START )

    }
  }

}
