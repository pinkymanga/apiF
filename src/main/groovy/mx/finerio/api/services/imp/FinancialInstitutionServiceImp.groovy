package mx.finerio.api.services.imp

import mx.finerio.api.domain.Client
import mx.finerio.api.domain.Customer
import mx.finerio.api.domain.FinancialInstitution
import mx.finerio.api.domain.FinancialInstitution.Status
import mx.finerio.api.domain.FinancialIntitutionSpecs
import mx.finerio.api.domain.Country
import mx.finerio.api.domain.repository.CountryRepository
import mx.finerio.api.domain.repository.FinancialInstitutionRepository
import mx.finerio.api.dtos.FinancialInstitutionListDto
import mx.finerio.api.exceptions.BadImplementationException
import mx.finerio.api.exceptions.BadRequestException
import mx.finerio.api.exceptions.InstanceNotFoundException
import mx.finerio.api.services.CustomerService
import mx.finerio.api.services.ListService
import mx.finerio.api.services.FinancialInstitutionService
import mx.finerio.api.services.SecurityService
import mx.finerio.api.validation.FinancialInstitutionUpdateCommand
import org.springframework.beans.factory.annotation.Autowired
import mx.finerio.api.validation.FinancialInstitutionCreateCommand
import mx.finerio.api.validation.ValidationCommand
import org.springframework.stereotype.Service

import javax.transaction.Transactional


@Service
class FinancialInstitutionServiceImp implements FinancialInstitutionService {

  @Autowired
  FinancialInstitutionRepository financialInstitutionRepository

  @Autowired
  ListService listService

  @Autowired
  CountryRepository countryRepository

  @Autowired
  SecurityService securityService

  @Autowired
  CustomerService customerService

  static final def defaultInstitutionType = FinancialInstitution.InstitutionType.PERSONAL
  static final def defaultCountry = 'MX'


  @Override
  FinancialInstitution create(FinancialInstitutionCreateCommand cmd) throws Exception {
    verifyBody(cmd)
    Customer customer = customerService.findOne(cmd.customerId)
    verifyUniqueCode(cmd, customer)
    verifyLoggedClient(customer?.client)

    FinancialInstitution financialInstitution = new FinancialInstitution()

    financialInstitution.with {
      code = cmd.code
      internalCode = cmd.internalCode
      description = cmd.description
      name = cmd.name
      provider = getProviderEnum(cmd.provider)
      status = getStatusEnum(cmd.status)
      institutionType = getInstitutionTypeEnum(cmd.institutionType)
      country = searchCountry(cmd.country)
      financialInstitution.customer = customer
      dateCreated = new Date()
      version = 0
    }

    financialInstitutionRepository.save(financialInstitution)
  }

  @Override
  Map findAll( Map params ) throws Exception {
    
   if ( params == null ) {
      throw new BadImplementationException(
          'financialInstitutionService.findAll.params.null' )
    }
 
    def dto = getFindAllDto( params )
    def spec = FinancialIntitutionSpecs.findAll( dto )
    return listService.findAll( dto, financialInstitutionRepository, spec )
    
  }

  @Override
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

  @Override
  FinancialInstitution getByIdAndCustomer(Long id, Customer customer) {
    verifyLoggedClient(customer?.client)
    Optional.ofNullable(financialInstitutionRepository
            .findByIdAndCustomerAndDateDeletedIsNull(id, customer))
            .orElseThrow({ -> new InstanceNotFoundException( 'financialInstitution.not.found' )})
  }

  @Override
  void delete(FinancialInstitution entity){
    entity.dateDeleted = new Date()
    financialInstitutionRepository.save(entity)
  }

  @Override
  @Transactional
  FinancialInstitution update(FinancialInstitutionUpdateCommand cmd, Long id) {
    verifyBody(cmd)
    FinancialInstitution financialEntity = findOne(id)
    if(!financialEntity.customer){
      throw new InstanceNotFoundException( 'financialInstitution.not.found' )
    }
    verifyLoggedClient(financialEntity?.customer?.client)
    verifyUniqueCode(cmd, financialEntity.customer)

    financialEntity.with {
      name = cmd.name ?: financialEntity.name
      code = cmd.code ?: financialEntity.code
      internalCode = cmd.internalCode ?: financialEntity.internalCode
      description = cmd.description ?: financialEntity.description
      provider = cmd.provider ? getProviderEnum(cmd.provider) : financialEntity.provider
      status = cmd.status ? getStatusEnum(cmd.status) : financialEntity.status
      institutionType = cmd.institutionType? getInstitutionTypeEnum(cmd.institutionType): financialEntity.institutionType
      country = cmd.country ? searchCountry(cmd.country) : financialEntity.country
      version = financialEntity.version + 1
    }

    financialInstitutionRepository.save(financialEntity)
  }

  @Override
  FinancialInstitution findOneByCode( String  code ) throws Exception {

    if ( code == null ) {
      throw new BadImplementationException(
          'financialInstitutionService.findOneByCode.code.null' )
    }
 
    def instance = financialInstitutionRepository.findOneByCode( code )

    if ( !instance ) {
      throw new InstanceNotFoundException( 'financialInstitution.not.found' )
    }
 
    instance

  }

  @Override
  FinancialInstitution findOneAndValidate( Long id ) throws Exception {

    if ( id == null ) {
      throw new BadImplementationException(
          'financialInstitutionService.findOneAndValidate.id.null' )
    }

    def financialInstitution = findOne( id )

    if ( financialInstitution.status !=
            FinancialInstitution.Status.ACTIVE
            && financialInstitution.status !=
            FinancialInstitution.Status.PARTIALLY_ACTIVE ) {
      throw new BadRequestException( 'financialInstitution.disabled' )
    }

    financialInstitution

  }

  @Override
  Map getFields( FinancialInstitution financialInstitution ) throws Exception {

    if ( !financialInstitution ) {
      throw new BadImplementationException(
          'financialInstitutionService.getFields.financialInstitution.null' )
    }

    [ id: financialInstitution.id, name: financialInstitution.name,
        code: financialInstitution.code,
        status: financialInstitution.status == Status.PARTIALLY_ACTIVE ?
        Status.ACTIVE : financialInstitution.status ]

  }

  private FinancialInstitutionListDto getFindAllDto( Map params ) throws Exception {

    def dto = new FinancialInstitutionListDto()
    listService.validateFindAllDto( dto, params )

    if( params.type ) {
      try{
         dto.type =
          FinancialInstitution.InstitutionType.valueOf(
              params.type.trim().toUpperCase() )
      }catch( IllegalArgumentException ex ){
        throw new BadRequestException(
            'financialInstitution.type.not.found' )
      }       
      
    }else{
      dto.type = defaultInstitutionType
    }

    Country country

    if( params.country ) {
      country = searchCountry( params.country )
    }else{
       dto.country = countryRepository.findOneByCode( defaultCountry )
    }
    
    dto

  }

  private Country searchCountry(String countryCode) {
    Optional.ofNullable(countryRepository.findOneByCode( countryCode ))
            .orElseThrow({ -> new BadRequestException('country.not.found') })
  }

  void verifyBody(ValidationCommand cmd) {
    if (!cmd) {
      throw new BadRequestException('request.body.invalid')
    }
  }

  private void verifyUniqueCode(ValidationCommand cmd, Customer customer) {
    if(findByCode(cmd, customer)){
      throw new BadRequestException('financialInstitution.code.nonUnique')
    }
  }

  private FinancialInstitution findByCode(ValidationCommand cmd, Customer customer) {
    financialInstitutionRepository.findByCodeAndCustomerAndDateDeletedIsNull(String.valueOf(cmd["code"]), customer)
  }

  private Status getStatusEnum(String status) {
    try {
     return Status.valueOf(status)
    }
    catch (IllegalArgumentException e) {
      throw new BadRequestException('financialInstitution.status.invalid')
    }
  }

  private FinancialInstitution.InstitutionType getInstitutionTypeEnum(String institutionType) {
    try {
      return FinancialInstitution.InstitutionType.valueOf(institutionType)
    }
    catch (IllegalArgumentException e) {
      throw new BadRequestException('financialInstitution.institutionType.invalid')
    }
  }

  private FinancialInstitution.Provider getProviderEnum(String provider) {
    try {
      return FinancialInstitution.Provider.valueOf(provider)
    }
    catch (IllegalArgumentException e) {
      throw new BadRequestException('financialInstitution.provider.invalid')
    }
  }

  private void verifyLoggedClient(Client client) {
    if (client !=  securityService.getCurrent()) {

      throw new javax.management.InstanceNotFoundException('account.notFound')
    }
  }

  private long verifyIdNull(long id) {
    Optional.ofNullable(id)
            .orElseThrow({ ->
              new InstanceNotFoundException('financialInstitutionService.findOne.id.null')
            })
  }

}