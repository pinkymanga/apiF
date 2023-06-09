package mx.finerio.api.services

import mx.finerio.api.domain.Client
import mx.finerio.api.domain.Callback
import mx.finerio.api.domain.repository.CallbackRepository
import mx.finerio.api.dtos.CallbackDto
import mx.finerio.api.exceptions.BadImplementationException
import mx.finerio.api.exceptions.BadRequestException

import spock.lang.Specification

class CallbackServiceSendToClientSpec extends Specification {

  def service = new CallbackService()

  def callbackService = Mock( CallbackService )
  def callbackRepository = Mock( CallbackRepository )
  def rsaCryptService = Mock( RsaCryptService  )
  def clientMtlsService = Mock( ClientMtlsService )

  def setup() {

    service.selfReference = callbackService
    service.callbackRepository = callbackRepository
    service.rsaCryptService = rsaCryptService
    service.clientMtlsService = clientMtlsService

  }

  def "invoking method successfully"() {

    when:
      service.sendToClient( client, nature, data )
    then:
      1 * callbackService.sendCallback( _ as String, _ as Map, _ as Map )
      1 * callbackRepository.findFirstByClientAndNatureAndDateDeletedIsNull(
              _ as Client, _ as Callback.Nature ) >> callback
    where:
      client = new Client()
      nature = Callback.Nature.SUCCESS
      data = [ hello: 'world' ]
      callback = new Callback(id: 1L, url: 'finerio.prueba.com')

  }

  def "instance not found"() {

    when:
      service.sendToClient( client, nature, data )
    then:
      0 * callbackService.sendCallback( _ as String, _ as Map, _ as Map )
    where:
      client = new Client()
      nature = Callback.Nature.SUCCESS
      data = [ hello: 'world' ]

  }

  def "parameter 'client' is null"() {

    when:
      service.sendToClient( client, nature, data )
    then:
      BadImplementationException e = thrown()
      e.message == 'callbackService.sendToClient.client.null'
    where:
      client = null
      nature = Callback.Nature.SUCCESS
      data = [ hello: 'world' ]

  }

  def "parameter 'nature' is null"() {

    when:
      service.sendToClient( client, nature, data )
    then:
      BadImplementationException e = thrown()
      e.message == 'callbackService.sendToClient.nature.null'
    where:
      client = new Client()
      nature = null
      data = [ hello: 'world' ]

  }

  def "parameter 'data' is null"() {

    when:
      service.sendToClient( client, nature, data )
    then:
      BadImplementationException e = thrown()
      e.message == 'callbackService.sendToClient.data.null'
    where:
      client = new Client()
      nature = Callback.Nature.SUCCESS
      data = null

  }

  def "parameter 'data' is empty"() {

    when:
      service.sendToClient( client, nature, data )
    then:
      BadImplementationException e = thrown()
      e.message == 'callbackService.sendToClient.data.null'
    where:
      client = new Client()
      nature = Callback.Nature.SUCCESS
      data = [:]

  }

}
