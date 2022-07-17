package dev.crashteam.bigboss.repository

import dev.crashteam.bigboss.repository.entity.AccountWalletEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountWalletRepository : JpaRepository<AccountWalletEntity, String>
