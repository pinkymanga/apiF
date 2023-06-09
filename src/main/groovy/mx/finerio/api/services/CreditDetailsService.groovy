package mx.finerio.api.services

import groovy.json.JsonBuilder

import mx.finerio.api.exceptions.BadImplementationException
import mx.finerio.api.exceptions.BadRequestException
import mx.finerio.api.exceptions.InstanceNotFoundException
import mx.finerio.api.domain.repository.*
import mx.finerio.api.domain.*
import mx.finerio.api.dtos.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreditDetailsService {

  @Autowired
  AccountService accountService

  @Autowired
  CreditDetailsRepository creditDetailsRepository

  CreditDetails create( CreditDetailsDto creditDetailsDto, Account account ) throws Exception {

    if ( !creditDetailsDto ) {
      throw new BadImplementationException(
          'creditDetailsService.create.creditDetailsDto.null' )
    }
 
    if ( !account ) {
      throw new BadImplementationException(
          'creditDetailsService.create.account.null' )
    }
    def flag = nullAll( creditDetailsDto )
    if( flag ){  
      return null
    }
    createInstance( creditDetailsDto, account )

  }

  CreditDetails findByAccountId( String accountId ) throws Exception {

    def account = accountService.findById( accountId )
    return creditDetailsRepository.
        findByCreditDetailsIdAccountAndDateDeletedIsNull( account )

  }

  Map getFields( CreditDetails creditDetails ) throws Exception {

    if ( !creditDetails ) {
      throw new BadImplementationException(
          'creditDetailsService.getFields.creditDetails.null' )
    }

    [
      creditLimit: creditDetails.limitCredit,
      closingDate: creditDetails.closingDate,
      lastClosingDate: creditDetails.lastClosingDate,
      nonInterestPayment: creditDetails.nonInterestPayment,
      statementBalance: creditDetails.statementBalance,
      minimumPayment: creditDetails.minimumPayment,
      dueDate: creditDetails.dueDate,
      annualPercentageRate: creditDetails.annualPercentageRate,
      cardNumber: creditDetails.cardNumber,
      availableBalance: creditDetails.availableBalance
    ]

  }

  @Transactional
  private CreditDetails createInstance( CreditDetailsDto creditDetailsDto, Account account ){

    def instance = creditDetailsRepository.findByCreditDetailsIdAccountAndDateDeletedIsNull( account ) 
    if( !instance ){ 
      instance = new CreditDetails()
      instance.dateCreated = new Date()
      def creditDetailsId = new CreditDetailsId( )
        creditDetailsId.account = account
      instance.creditDetailsId = creditDetailsId
    }
    instance.closingDate = creditDetailsDto.closing_date ? new Date().parse( "yyyy-MM-dd'T'HH:mm:ss",
        creditDetailsDto.closing_date ) : null
    instance.nonInterestPayment = creditDetailsDto.non_interest_payment
    instance.statementBalance = creditDetailsDto.statement_balance
    instance.minimumPayment = creditDetailsDto.minimum_payment  
    instance.limitCredit = creditDetailsDto.credit_limit 
    instance.dueDate = creditDetailsDto.due_date ? new Date().parse( "yyyy-MM-dd'T'HH:mm:ss",
        creditDetailsDto.due_date ) : null
    instance.lastClosingDate = creditDetailsDto.last_closing_date ? new Date().parse( "yyyy-MM-dd'T'HH:mm:ss",
        creditDetailsDto.last_closing_date ) : null
    instance.annualPercentageRate = creditDetailsDto.annual_porcentage_rate
    instance.cardNumber = creditDetailsDto.card_number
    instance.availableBalance = creditDetailsDto.available_balance
    instance.lastUpdated = new Date()
    creditDetailsRepository.save( instance )
    instance
  
  }

  private Boolean nullAll( CreditDetailsDto creditDetailsDto ){
    def flag = true
    if( creditDetailsDto.closing_date
    || creditDetailsDto.non_interest_payment
    || creditDetailsDto.statement_balance
    || creditDetailsDto.minimum_payment
    || creditDetailsDto.credit_limit
    || creditDetailsDto.due_date
    || creditDetailsDto.closing_date
    || creditDetailsDto.annual_porcentage_rate
    || creditDetailsDto.card_number
    || creditDetailsDto.available_balance
    ){ flag = false }  
    flag  
  }

  String maskCardNumber( String rawCardNumber ) throws Exception {

    if ( rawCardNumber == null || rawCardNumber == '' ) {
      return rawCardNumber
    }

    return "${rawCardNumber.reverse().take( 4 )}XXXX".reverse()

  }

}
