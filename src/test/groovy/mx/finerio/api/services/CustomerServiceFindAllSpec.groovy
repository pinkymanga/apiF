package mx.finerio.api.services

import mx.finerio.api.domain.Client
import mx.finerio.api.domain.Customer
import mx.finerio.api.domain.repository.CustomerRepository
import mx.finerio.api.dtos.ListDto
import mx.finerio.api.exceptions.BadImplementationException
import mx.finerio.api.exceptions.BadRequestException

import org.springframework.data.jpa.repository.JpaRepository

import spock.lang.Specification

class CustomerServiceFindAllSpec extends Specification {

  def service = new CustomerService()

  def listService = Mock( ListService )
  def securityService = Mock( SecurityService )
  def customerRepository = Mock( CustomerRepository )

  def setup() {

    service.listService = listService
    service.securityService = securityService
    service.customerRepository = customerRepository

  }

  def "invoking method successfully"() {

    when:
      def result = service.findAll( params )
    then:
      1 * listService.validateFindAllDto( _ as ListDto, _ as Map )
      2 * securityService.getCurrent() >> client
      1 * customerRepository.findOne( _ as Long )>>
          new Customer( client: client )
      1 * listService.findAll( _ as ListDto, _ as JpaRepository,
          _ as Object ) >> [ data: [ new Customer(), new Customer() ],
          nextCursor: 'nextCursor' ]
      result instanceof Map
      result.next == null
      result.data instanceof List
      result.data.size() == 2
      result.data[ 0 ] instanceof Customer
    where:
      params = getParams()
      client = new Client( id: 1 )

  }

  def "parameter 'params' is null"() {

    when:
      service.findAll( params )
    then:
      BadImplementationException e = thrown()
      e.message == 'customerService.findAll.params.null'
    where:
      params = null

  }

  def "parameter 'params.cursor' is null"() {

    given:
      params.cursor = null
    when:
      def result = service.findAll( params )
    then:
      1 * listService.validateFindAllDto( _ as ListDto, _ as Map )
      1 * securityService.getCurrent() >> client
      1 * listService.findAll( _ as ListDto, _ as JpaRepository,
          _ as Object ) >> [ data: [ new Customer(), new Customer() ],
          nextCursor: 'nextCursor' ]
      result instanceof Map
      result.next == null
      result.data instanceof List
      result.data.size() == 2
      result.data[ 0 ] instanceof Customer
    where:
      params = getParams()
      client = new Client( id: 1 )

  }

  def "parameter 'params.cursor' is invalid"() {

    given:
      params.cursor = 'invalid'
    when:
      def result = service.findAll( params )
    then:
      1 * listService.validateFindAllDto( _ as ListDto, _ as Map )
      BadRequestException e = thrown()
      e.message == 'cursor.invalid'
    where:
      params = getParams()
      client = new Client( id: 1 )

  }

  private Map getParams() throws Exception {

    [
      cursor: '1'
    ]

  }

}
