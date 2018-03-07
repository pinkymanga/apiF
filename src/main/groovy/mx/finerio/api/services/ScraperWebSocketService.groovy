package mx.finerio.api.services

import javax.websocket.ClientEndpoint
import javax.websocket.ContainerProvider
import javax.websocket.OnError
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ScraperWebSocketService {

  def session
  
  ScraperWebSocketService( @Value('${scraper.ws.path}') String url ) {

    def container = ContainerProvider.webSocketContainer
    session = container.connectToServer( ScraperClientEndpointService,
        URI.create( url ) )
    
  }
  
  void send( String message ) throws Exception {
    session.basicRemote.sendText( message )
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

