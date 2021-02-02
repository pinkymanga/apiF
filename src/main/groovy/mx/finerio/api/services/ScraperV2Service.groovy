package mx.finerio.api.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import mx.finerio.api.dtos.CreateCredentialDto
import java.time.LocalDate
import org.springframework.beans.factory.annotation.Autowired

@Service
class ScraperV2Service {

	@Autowired
    RsaCryptScraperV2Service rsaCryptScraperV2Service

    @Autowired
    CallbackGatewayClientService callbackGatewayClientService

	@Autowired
    ScraperV2ClientService scraperV2ClientService

    @Value('${gateway.source}')
	String source

	void createCredential( CreateCredentialDto createCredentialDto ) throws Exception {
             			
		def jsonMap = [:]
		jsonMap.username = createCredentialDto.username
		jsonMap.password = createCredentialDto.password
		
		def state = createCredentialDto.credentialId		
		def jsonString = JsonOutput.toJson( jsonMap )				
		def jsonBase64 = jsonString.getBytes( 'UTF-8' ).encodeBase64().toString()		
		def jsonEncrypted = rsaCryptScraperV2Service.encrypt( jsonBase64 )
		
		LocalDate now = LocalDate.now()		
		String endDate = now.toString()		
	    String startDate = now.minusMonths( 2 ).toString()
							    
		def finalData = [ institution: createCredentialDto.bankCode,
			data: jsonEncrypted,
			state: state,
			start_date: startDate,
			end_date: endDate ]

		callbackGatewayClientService
			.registerCredential( [ credentialId: state ,source: source ] )
		         	
		 scraperV2ClientService.createCredential( finalData )									
		
	}

}
