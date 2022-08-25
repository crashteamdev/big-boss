package dev.crashteam.bigboss.repository

import dev.crashteam.bigboss.repository.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountRepository : JpaRepository<AccountEntity, Long> {

    fun findByUserId(userId: String): AccountEntity?

    fun deleteByUserId(userId: String)

}
