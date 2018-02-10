package mx.finerio.api.services

import javax.validation.Valid

import mx.finerio.api.domain.Callback
import mx.finerio.api.domain.repository.CallbackRepository
import mx.finerio.api.dtos.CallbackDto
import mx.finerio.api.dtos.CallbackUpdateDto
import mx.finerio.api.exceptions.BadImplementationException
import mx.finerio.api.exceptions.BadRequestException
import mx.finerio.api.exceptions.InstanceNotFoundException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class CallbackService {

  @Autowired
  SecurityService securityService

  @Autowired
  CallbackRepository callbackRepository

  Callback create( CallbackDto callbackDto ) throws Exception {

    if ( !callbackDto ) {
      throw new BadImplementationException(
          'callbackService.create.callbackDto.null' )
    }
 
    def client = securityService.getCurrent()

    if ( callbackRepository.findByClientAndNature(
        client, callbackDto.nature ) ) {
      throw new BadRequestException( 'callback.create.exists' )
    }
 
    def instance = new Callback()
    instance.url = callbackDto.url
    instance.nature = callbackDto.nature
    instance.client = client
    def now = new Date()
    instance.dateCreated = now
    instance.lastUpdated = now
    callbackRepository.save( instance )
    instance

  }

  Map findAll() throws Exception {
    [ data: callbackRepository.findAll(), nextCursor: null ]
  }

  Callback findOne( Long id ) throws Exception {

    if ( id == null ) {
      throw new BadImplementationException(
          'callbackService.findOne.id.null' )
    }
 
    def client = securityService.getCurrent()
    def instance = callbackRepository.findOne( id )

    if ( !instance || instance.client.id != client.id ) {
      throw new InstanceNotFoundException( 'callback.not.found' )
    }
 
    instance

  }

  Callback update( Long id, CallbackUpdateDto callbackUpdateDto )
      throws Exception {

    if ( id == null ) {
      throw new BadImplementationException(
          'callbackService.update.id.null' )
    }
 
    if ( !callbackUpdateDto ) {
      throw new BadImplementationException(
          'callbackService.update.callbackUpdateDto.null' )
    }
 
    def instance = findOne( id )
    instance.url = callbackUpdateDto.url
    instance.lastUpdated = new Date()
    callbackRepository.save( instance )

  }

  Map getFields( Callback callback ) throws Exception {

    if ( !callback ) {
      throw new BadImplementationException(
          'callbackService.getFields.callback.null' )
    }
 
    [ id: callback.id, url: callback.url, nature: callback.nature,
        dateCreated: callback.dateCreated, lastUpdated: callback.lastUpdated ]

  }

}
