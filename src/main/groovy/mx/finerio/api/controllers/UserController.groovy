package mx.finerio.api.controllers

import mx.finerio.api.services.CredentialService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam


@RestController
class UserController {

  @Autowired
  CredentialService credentialService

	@PostMapping( '/credential' )
  ResponseEntity createCredential() {
println "\ncontroller\n"
    ResponseEntity.ok( [ result: credentialService.loginCredential() ] )
  }


}
