package mx.finerio.api.aop

import mx.finerio.api.domain.*

import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.stereotype.Component

@Component
@Aspect
class ConceptServiceCreate {

  final static Logger log = LoggerFactory.getLogger(
      'mx.finerio.api.aop.ConceptServiceCreate' )

  @Pointcut(
    value='execution(mx.finerio.api.domain.Concept mx.finerio.api.services.ConceptService.create(..)) && bean(conceptService) && args(movementId, attributes)',
    argNames='movementId, attributes'
  )
  public void create(String movementId, Map attributes ) {}

  @Before('create(movementId, attributes)')
  void before(String movementId, Map attributes ) {
    log.info( "<< movementId: {}, attributes: {}", movementId, attributes )
  }

  @AfterReturning(
    pointcut='create(String, java.util.Map)',
    returning='response'
  )
  void afterReturning( Concept response ) {
    log.info( '>> response: {}', response )
  }

  @AfterThrowing(
    pointcut='create(String, java.util.Map)',
    throwing='e'
  )
  void afterThrowing( Exception e ) {
    log.info( "XX ${e.class.simpleName} - ${e.message}" )
  }

}
