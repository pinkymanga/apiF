package mx.finerio.api.services

import mx.finerio.api.domain.Callback
import mx.finerio.api.domain.FinancialInstitution
import mx.finerio.api.domain.repository.FinancialInstitutionRepository
import mx.finerio.api.dtos.BankStatusDto
import mx.finerio.api.exceptions.BadRequestException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BankStatusService {

  @Autowired
  CallbackService callbackService

  @Autowired
  FinancialInstitutionRepository financialInstitutionRepository

  @Transactional
  void changeStatus( BankStatusDto dto ) throws Exception {

    if ( dto == null ) {
      throw new IllegalArgumentException(
          'bankStatusService.changeBankStatus.dto.null' )
    }

    def bank = getBank( dto.bankId )
    bank.status = getStatus( dto.status )
    financialInstitutionRepository.save( bank )
    notifyClients( dto )

  }

  private FinancialInstitution getBank( Long bankId ) throws Exception {

    def bank = financialInstitutionRepository.findById( bankId )

    if ( bank == null ) {
      throw new BadRequestException( 'bank.id.not.found' )
    }

    return bank

  }

  private FinancialInstitution.Status getStatus( String statusString )
      throws Exception {

    def status = null

    try {
      status = FinancialInstitution.Status.valueOf( statusString )
    } catch ( IllegalArgumentException e ) {
      throw new BadRequestException( 'bank.status.not.found' )
    }

    return status

  }

  private void notifyClients( BankStatusDto dto ) throws Exception {

    if ( !dto.notifyClients ) { return }
    def callbacks = callbackService.findAllByNature( Callback.Nature.BANKS )
    def data = [
      bankId: dto.bankId,
      status: dto.status
    ]

    for ( callback in callbacks ) {
      callbackService.sendToClient( callback.client, Callback.Nature.BANKS,
          data )
    }

  }

}