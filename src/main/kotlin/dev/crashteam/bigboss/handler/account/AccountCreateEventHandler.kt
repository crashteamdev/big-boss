package dev.crashteam.bigboss.handler.account

import dev.crashteam.bigboss.repository.AccountRepository
import dev.crashteam.bigboss.repository.entity.AccountEntity
import dev.crashteam.subscription.event.AccountEvent
import org.springframework.stereotype.Component

@Component
class AccountCreateEventHandler(
    private val accountRepository: AccountRepository
) : AccountEventHandler {

    override fun handle(accountEvents: List<AccountEvent>) {
        for (accountEvent in accountEvents) {
            val foundedAccountEntity = accountRepository.findByUserId(accountEvent.eventSource.userId)

            if (foundedAccountEntity != null) continue

            val accountCreated = accountEvent.payload.accountCreated
            val accountEntity = AccountEntity().apply {
                this.userId = accountEvent.eventSource.userId
                this.email = accountCreated.email
                this.blocked = false
            }
            accountRepository.save(accountEntity)
        }
    }

    override fun isHandle(accountEvent: AccountEvent): Boolean {
        return accountEvent.payload.hasAccountCreated()
    }
}
