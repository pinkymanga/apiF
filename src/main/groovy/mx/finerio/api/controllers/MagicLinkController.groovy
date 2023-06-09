package mx.finerio.api.controllers

import mx.finerio.api.dtos.CreateCredentialV2Dto
import mx.finerio.api.dtos.CredentialDto
import mx.finerio.api.dtos.CredentialInteractiveDto
import mx.finerio.api.dtos.CredentialInteractiveMagicLinkDto
import mx.finerio.api.services.BankFieldService
import mx.finerio.api.services.MagicLinkService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@RestController
class MagicLinkController {

    @Autowired
    MagicLinkService magicLinkService

    @PostMapping('/magicLink/customers/{customerId}/email/magicLink')
    ResponseEntity sendMagicLink( @PathVariable Long customerId ) {
        magicLinkService.sendMagicLink( customerId )
        new ResponseEntity( HttpStatus.NO_CONTENT )
    }

    @GetMapping('/magicLink/{customerLinkId}/banks')
    ResponseEntity findBanksByCustomerLinkId( @PathVariable String customerLinkId ) {
        def response = magicLinkService
                .findBanksByCustomerLinkId( customerLinkId )
        new ResponseEntity( response, HttpStatus.OK )
    }

    @GetMapping('/magicLink/{customerLinkId}/banks/{bankId}/fields')
    ResponseEntity findAll( @PathVariable String customerLinkId,
                            @PathVariable Long bankId ) {
        def response = magicLinkService
                .findBankFieldsByCustomerLinkIdAndBankId( customerLinkId, bankId )
        new ResponseEntity( response, HttpStatus.OK )
    }

    @PostMapping('/magicLink/{customerLinkId}/credentials')
    ResponseEntity createCredential( @PathVariable  String customerLinkId,
                                     @RequestBody @Valid CreateCredentialV2Dto createCredentialV2Dto  ) {
        def response = magicLinkService
                .createCredential( customerLinkId, createCredentialV2Dto )
        new ResponseEntity( response, HttpStatus.CREATED )
    }

    @PostMapping('/magicLink/{customerLinkId}/credentials/{credentialId}/interactive')
    ResponseEntity sendInteractive( @PathVariable  String customerLinkId,
                                    @PathVariable  String credentialId,
                                    @RequestBody @Valid CredentialInteractiveMagicLinkDto credentialInteractiveMagicLinkDto  ) {
        magicLinkService
                .sendInteractive( customerLinkId, credentialId, credentialInteractiveMagicLinkDto )

        ResponseEntity.accepted().build()
    }

}
