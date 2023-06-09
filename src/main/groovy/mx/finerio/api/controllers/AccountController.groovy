package mx.finerio.api.controllers

import mx.finerio.api.domain.CreditDetails
import mx.finerio.api.services.AccountBalanceService
import mx.finerio.api.services.AccountDetailsService
import mx.finerio.api.services.AccountService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class AccountController {

  @Autowired
  AccountBalanceService accountBalanceService

  @Autowired
  AccountService accountService

  @Autowired
  AccountDetailsService accountDetailsService

  @GetMapping('/accounts')
  ResponseEntity findAll( @RequestParam Map<String, String> params ) {

    def response = accountService.findAll( params )
    response.data = response.data.collect {
        accountService.getFields( it ) }
    new ResponseEntity( response, HttpStatus.OK )

  }

  @GetMapping('/accounts/{id}/details')
  ResponseEntity findAllDetails( @PathVariable String id ) {

    def accountDetailsMap = accountDetailsService.findAllByAccount( id )
    new ResponseEntity( accountDetailsMap, HttpStatus.OK )

  }

  @GetMapping('/accounts/{id}/balance')
  ResponseEntity getBalance( @PathVariable String id ) {

    def balance = accountBalanceService.getBalanceByAccount( id )
    new ResponseEntity( balance, HttpStatus.OK )

  }

}
