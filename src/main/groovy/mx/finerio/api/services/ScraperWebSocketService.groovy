package mx.finerio.api.services

import javax.websocket.ClientEndpoint
import javax.websocket.ContainerProvider
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session

import mx.finerio.api.dtos.FailureCallbackData
import mx.finerio.api.dtos.FailureCallbackDto
import mx.finerio.api.dtos.ScraperWebSocketSendDto
import mx.finerio.api.exceptions.BadImplementationException

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScraperWebSocketService {

  @Value('${scraper.ws.path}')
  String url

  @Value('${scraper.ws.timeout.message}')
  String timeoutMessage

  @Autowired
  ScraperCallbackService scraperCallbackService

  def sessions = [:]
  
  void send( ScraperWebSocketSendDto scraperWebSocketSendDto )
      throws Exception {

    if ( !scraperWebSocketSendDto ) {
      throw new BadImplementationException(
          'scraperWebSocketService.send.scraperWebSocketSendDto.null' )
    }

    if ( scraperWebSocketSendDto.destroyPreviousSession ) {

      closeSession( scraperWebSocketSendDto.id )
      sessions[ scraperWebSocketSendDto.id ] = [ session: createSession(),
          time: new Date().time ]

    }

    if ( !sessions[ scraperWebSocketSendDto.id ] ) {
      return
    }

    sessions[ scraperWebSocketSendDto.id ]?.session?.basicRemote?.sendText(
        scraperWebSocketSendDto.message )

  }
  
  void closeSession( String id ) throws Exception {

    if ( !id ) {
      throw new BadImplementationException(
          'scraperWebSocketService.closeSession.id.null' )
    }

    sessions.remove( id )?.session?.close()

  }

  @Scheduled(cron = '0 * * * * *')
  void destroySessions() throws Exception {

    def nowMillis = new Date().time

    sessions.findResults {

      if ( nowMillis - it.value.time >= 60000 ) {
        it.value.session.basicRemote
            .sendText( '{"data":{"stage":"logout"}}' )
        return it.key
      }

      null

    }.each { setCredentialFailure( it ) }

  }

  private Session createSession() throws Exception {

    def container = ContainerProvider.webSocketContainer
    container.connectToServer( ScraperClientEndpointService,
        URI.create( url ) )

  }

  private void setCredentialFailure( String credentialId ) throws Exception {

    scraperCallbackService.processFailure( new FailureCallbackDto(
      data: new FailureCallbackData(
        credential_id: credentialId,
        error_message: timeoutMessage
    ) ) )

  }

}

@ClientEndpoint
class ScraperClientEndpointService {

  final static Logger log = LoggerFactory.getLogger(
      'mx.finerio.api.services.ScraperClientEndpointService' )

  @OnOpen
  void onOpen( Session session ) {
    log.info( "<< session opened: {}", session.id )
  }

  @OnMessage
  void processMessage( byte[] message ) {
    log.info( "<< message: {}", new String( message, 'UTF-8' ) )
  }

  @OnError
  void processError( Throwable t ) {
    log.info( "XX ${t.class.simpleName} - ${t.message}" )
  }

}
