package mx.finerio.api.controllers

import javax.validation.Valid

import mx.finerio.api.dtos.BankStatusDto
import mx.finerio.api.services.BankFieldService
import mx.finerio.api.services.BankStatusService
import mx.finerio.api.services.FinancialInstitutionService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@RestController
class FinancialInstitutionController {

  @Autowired
  BankFieldService bankFieldService

  @Autowired
  BankStatusService bankStatusService

  @Autowired
  FinancialInstitutionService financialInstitutionService

  @GetMapping('/banks')
  ResponseEntity findAll( @RequestParam Map<String, String> params ) {
  
    def response = financialInstitutionService.findAll(params)
    response = response.data.collect {
        financialInstitutionService.getFields( it ) }
    new ResponseEntity( response, HttpStatus.OK )

  }

  @GetMapping('/banks/{id}/fields')
  ResponseEntity findAll( @PathVariable Long id ) {
  
    def response = bankFieldService.findAllByFinancialInstitution( id )
    response = response.collect { bankFieldService.getFields( it ) }
    response = response.sort { it.position }
    new ResponseEntity( response, HttpStatus.OK )

  }

  @PutMapping('/Jc79e49K964wK6pBWsHW6hw9SUW5jYytb8NR9Q8ZwrVpSrXFdK')
  ResponseEntity changeStatus( @RequestBody @Valid BankStatusDto dto ) {
  
    def response = bankStatusService.changeStatus( dto )
    new ResponseEntity( HttpStatus.NO_CONTENT )

  }

}
