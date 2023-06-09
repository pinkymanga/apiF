package mx.finerio.api.domain

import javax.persistence.*
import javax.validation.constraints.*

import groovy.transform.ToString

@Entity
@Table(name = 'customers')
@ToString(includePackage = false, includeNames = true)
class Customer {
  
  @Id @GeneratedValue
  @Column(name = 'id', updatable = false)
  Long id

  @NotNull
  @Size(min = 1, max = 50)
  @Column(name = 'name')
  String name

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = 'client_id')
  Client client

  @NotNull
  @Column(name = 'date_created')
  Date dateCreated

  @Column(name = 'date_deleted', nullable = true)
  Date dateDeleted

}
