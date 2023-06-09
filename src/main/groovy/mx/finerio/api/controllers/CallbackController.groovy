package mx.finerio.api.controllers

import javax.validation.Valid

import mx.finerio.api.dtos.CallbackDto
import mx.finerio.api.dtos.CallbackUpdateDto
import mx.finerio.api.services.CallbackService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CallbackController {

  @Autowired
  CallbackService callbackService

  @PostMapping('/clients/callbacks')
  ResponseEntity create( @RequestBody @Valid CallbackDto callbackDto ) {
  
    def instance = callbackService.create( callbackDto )
    instance = callbackService.getFields( instance )
    new ResponseEntity( instance, HttpStatus.CREATED )

  }

  @GetMapping('/clients/callbacks')
  ResponseEntity findAll() {
  
    def response = callbackService.findAll()
    response.data = response.data.collect {
        callbackService.getFields( it ) }
    new ResponseEntity( response, HttpStatus.OK )

  }

  @GetMapping('/clients/callbacks/{id}')
  ResponseEntity findOne( @PathVariable Long id ) {

    def instance = callbackService.findOne( id )
    instance = callbackService.getFields( instance )
    new ResponseEntity( instance, HttpStatus.OK )

  }

  @PutMapping('/clients/callbacks/{id}')
  ResponseEntity update( @PathVariable Long id,
      @RequestBody @Valid CallbackUpdateDto callbackUpdateDto ) {

    def instance = callbackService.update( id, callbackUpdateDto )
    instance = callbackService.getFields( instance )
    new ResponseEntity( instance, HttpStatus.OK )

  }

  @DeleteMapping('/clients/callbacks/{id}')
  ResponseEntity delete( @PathVariable Long id ) {

    callbackService.delete( id )
    new ResponseEntity( HttpStatus.NO_CONTENT )

  }

}
