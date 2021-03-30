package mx.finerio.api.services

  import mx.finerio.api.domain.FinancialInstitution
import mx.finerio.api.domain.repository.FinancialInstitutionRepository
import mx.finerio.api.domain.repository.CountryRepository
import mx.finerio.api.exceptions.BadImplementationException
import mx.finerio.api.exceptions.BadRequestException
import mx.finerio.api.exceptions.InstanceNotFoundException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import mx.finerio.api.dtos.FinancialInstitutionListDto
import mx.finerio.api.domain.FinancialIntitutionSpecs

@Service
class FinancialInstitutionService {

  @Autowired
  FinancialInstitutionRepository financialInstitutionRepository

  @Autowired
  ListService listService

  @Autowired
  CountryRepository countryRepository

  static final def defaultInstitutionType = FinancialInstitution.InstitutionType.PERSONAL
  static final def defaultCountry = 'MX'


  Map findAll( Map params ) throws Exception {
    
   if ( params == null ) {
      throw new BadImplementationException(
          'financialInstitutionService.findAll.params.null' )
    }
 
    def dto = getFindAllDto( params )
    def spec = FinancialIntitutionSpecs.findAll( dto )
    listService.findAll( dto, financialInstitutionRepository, spec )    
    
  }

  FinancialInstitution findOne( Long id ) throws Exception {

    if ( id == null ) {
      throw new BadImplementationException(
          'financialInstitutionService.findOne.id.null' )
    }
 
    def instance = financialInstitutionRepository.findOne( id )

    if ( !instance ) {
      throw new InstanceNotFoundException( 'financialInstitution.not.found' )
    }
 
    instance

  }

  FinancialInstitution findOneAndValidate( Long id ) throws Exception {

    if ( id == null ) {
      throw new BadImplementationException(
          'financialInstitutionService.findOneAndValidate.id.null' )
    }

    def financialInstitution = findOne( id )

    if ( financialInstitution.status !=
        FinancialInstitution.Status.ACTIVE ) {
      throw new BadRequestException( 'financialInstitution.disabled' )
    }

    financialInstitution

  }

  Map getFields( FinancialInstitution financialInstitution ) throws Exception {

    if ( !financialInstitution ) {
      throw new BadImplementationException(
          'financialInstitutionService.getFields.financialInstitution.null' )
    }

    [ id: financialInstitution.id, name: financialInstitution.name,
        code: financialInstitution.code,
        status: financialInstitution.status ]

  }

  private FinancialInstitutionListDto getFindAllDto( Map params ) throws Exception {

    def dto = new FinancialInstitutionListDto()
    listService.validateFindAllDto( dto, params )

    if( params.institutionType ) {     
      try{
         dto.institutionType =
          FinancialInstitution.InstitutionType.valueOf( params.institutionType.trim().toUpperCase() )
      }catch( IllegalArgumentException ex ){
          throw new 
          BadImplementationException('financialInstitutionService.getFindAllDto.institutionType.notFound' ) 
      }       
      
    }else{
      dto.institutionType = defaultInstitutionType
    }


    def country

    if( params.country ) { 

      country = countryRepository.findOneByCode( params.country )

      if( country ) {
        dto.country = country   
      } else {
        throw new BadImplementationException(
          'financialInstitutionService.getFindAllDto.country.notFound' )  
      }
      
    }else{
       dto.country = countryRepository.findOneByCode( defaultCountry )
    }
    
    dto

  }

}
