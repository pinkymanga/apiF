package mx.finerio.api.services


import mx.finerio.api.domain.*
import mx.finerio.api.domain.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.microsoft.azure.servicebus.*
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder
import mx.finerio.api.dtos.TransactionDto
import java.util.concurrent.*
import groovy.json.JsonBuilder
import static java.nio.charset.StandardCharsets.*
import java.time.Duration
import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value

@Service
class AzureQueueService {


  final static Logger log = LoggerFactory.getLogger(
    'mx.finerio.api.services.AzureQueueService' )
 
  final String serviceUrl
  final String serviceName
  final Integer timeToLive
  ConnectionStringBuilder connection
  IMessageSender sendClient

  @Autowired
  public AzureQueueService (@Value('${servicebus.azure.transactions.stringConnection}') final String serviceUrl, 
    @Value('${servicebus.azure.transactions.name}') final String serviceName,
    @Value('${servicebus.azure.transactions.susbscriber.timeToLiveMinutes}') final String timeToLive ){

    this.serviceUrl = serviceUrl
    this.serviceName = serviceName
    this.timeToLive = timeToLive as Integer

    connection = new ConnectionStringBuilder( serviceUrl, serviceName )
    sendClient = ClientFactory.createMessageSenderFromConnectionStringBuilder( connection )

  }

  void queueTransactions( TransactionDto transactionDto, TransactionMessageType transactionMessageType  ) throws Exception {
    sendMessagesAsync( transactionDto, transactionMessageType )
  }

  private void sendMessagesAsync( TransactionDto transactionDto, TransactionMessageType type ) {

    String randomNumber = "-" + ( ( Math.random() * 1000 ) ) as String 
    Message message = new Message( new JsonBuilder( transactionDto ).toPrettyString().getBytes( UTF_8) )

    message.contentType='application/json'
    message.label = type.name()
    message.messageId = ( new Date().getTime() as String ) + randomNumber 
    message.timeToLive = Duration.ofMinutes( timeToLive ) 
    message.sessionId = transactionDto?.data?.credential_id
 
    log.info( "Sending message to transactions queue >> Id:${message.getMessageId()}, sessionId:${message.getSessionId()}, type:${message.getLabel()}" )
      sendClient.sendAsync( message ).thenRunAsync( {
        log.info( "Message acknowledged in transactions queue << Id:${message.getMessageId()}, sessionId:${message.getSessionId()}, type:${message.getLabel()}" )
      })  
        
  }


}
