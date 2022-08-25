package dev.crashteam.bigboss.service

import dev.crashteam.bigboss.repository.AccountRepository
import dev.crashteam.bigboss.repository.entity.AccountEntity
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class AccountService(
    private val accountRepository: AccountRepository
) {

    @Transactional
    fun getAccount(userId: String): AccountEntity? {
        return accountRepository.findByUserId(userId)
    }

}
