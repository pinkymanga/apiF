package mx.finerio.api.controllers

import mx.finerio.api.services.MovementService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
class MovementController {

  @Autowired
  MovementService movementService

  @GetMapping( '/movements/{id}' )
  ResponseEntity getMovement( @PathVariable String id, Pageable pageable ) {
    movementService.findByAccount( id, pageable ).content
        .collect { [id: it.id] }
    ResponseEntity.accepted().build()

  }


}