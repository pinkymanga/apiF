package mx.finerio.api.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

import mx.finerio.api.domain.*

interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor {
  
  User findOneByUsername( String username )
  User findById( String id )

}
