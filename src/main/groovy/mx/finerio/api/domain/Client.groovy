package mx.finerio.api.domain

import javax.persistence.*

import groovy.transform.ToString

import org.springframework.security.core.userdetails.UserDetails

import java.sql.Date

@Entity
@Table(name = 'clients')
@ToString(includePackage = false, includeNames = true, includes = ['id', 'username'])
class Client implements UserDetails {
  
  @Id 
  @Column(name = 'id', nullable = false, updatable = false)
  String id

  @Column(name = 'username', nullable = false, length = 50, unique = true)
  String username

  @Column(name = 'password', nullable = false, length = 255)
  String password

  @Column(name = 'enabled', nullable = false)
  boolean enabled

  @Column(name = 'account_non_expired', nullable = false)
  boolean accountNonExpired

  @Column(name = 'account_non_locked', nullable = false)
  boolean accountNonLocked

  @Column(name = 'credentials_non_expired', nullable = false)
  boolean credentialsNonExpired

  @Column(name = 'categorize_transactions', nullable = false)
  boolean categorizeTransactions

  @Column(name = 'use_transactions_table', nullable = false)
  boolean useTransactionsTable

  @Column(name = 'email')
  String email

  @Column(name = 'name')
  String name

  @Column(name = 'company')
  String company

  @Column(name = 'user_agent', nullable = true)
  String userAgent

  @Column(name = 'date_deleted')
  Date dateDeleted

  @Column(name = 'insights_enabled')
  Boolean insightsEnabled

  @Transient
  List authorities

}
