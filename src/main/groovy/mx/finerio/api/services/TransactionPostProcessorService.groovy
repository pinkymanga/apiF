package mx.finerio.api.services

import mx.finerio.api.domain.*
import mx.finerio.api.dtos.*
import mx.finerio.api.exceptions.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionPostProcessorService {

  @Autowired
  MovementService movementService

  @Autowired
  ConceptService conceptService

  @Autowired
  TransactionsApiService transactionsApiService

  Movement processDuplicated( Movement movement ) throws Exception {

    if ( !movement || !movement.id) {
      throw new BadRequestException( 'transactionPostProcessor.processDuplicated.movement.null' )
    }
    def mov  = movementService.findOne( movement.id )
    updateTransference( mov )
    if( mov.type == Movement.Type.DEPOSIT ){
       if( mov.account.nature && mov.account.nature == "Cr\u00E9dito" ){
        mov = movementService.updateDuplicated( mov )
        return mov  
      }
    }
    if( mov.type == Movement.Type.CHARGE ){
      def concept  = conceptService.findByMovement( movement )
      if( concept.category?.name == "Cajero automático" ){
        mov = movementService.updateDuplicated( mov )
        return mov  
      }
    }
    mov
  }

  private void updateTransference( Movement mov ){
    def tp
    if( mov.type == Movement.Type.DEPOSIT ){ tp = Movement.Type.CHARGE }
    if( mov.type == Movement.Type.CHARGE ){ tp = Movement.Type.DEPOSIT }
    List movsTransf  = movementService.getMovementsToTransference( mov.id, tp )
      if( movsTransf ){
        def dateMinus = mov.date.minus( 5 )
        List list = movsTransf.findAll{ it.date <= mov.date && it.date >= dateMinus }
        if(list){
          def descriptions = list.collect{ it.description }
          descriptions << mov.description
println "descriptions"
descriptions.each{ println it }
         def result = transactionsApiService.findTransference( list )
println "result"
result.each{ println it }
           
        }
      }


  }

}
