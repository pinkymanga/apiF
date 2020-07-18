package mx.finerio.api.domain

import groovy.transform.ToString
import javax.persistence.*
import javax.validation.constraints.*


@Entity
@Table(name = 'credential_token')
@ToString(excludes = 'password, securityCode, iv', includeNames = true, includePackage = false)
class CredentialToken {
 
  @Id @GeneratedValue
  @Column(name = 'id', updatable = false)
  Long id

  @Column(name = 'token_client_id', nullable = false, length = 255)
  String tokenClientId

  @Column(name = 'credential_id', nullable = false, length = 255)
  String credentialId

  @Column(name = 'date_created', nullable = false)
  Date dateCreated

  @Column(name = 'last_updated', nullable = false)
  Date lastUpdated

  @Column(name = 'date_deleted', nullable = true)
  Date dateDeleted

  @Column(name = 'version', nullable = false)
  Long version = 0


}
