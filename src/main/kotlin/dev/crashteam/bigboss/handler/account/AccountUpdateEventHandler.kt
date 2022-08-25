package dev.crashteam.bigboss.handler.account

import dev.crashteam.bigboss.repository.AccountRepository
import dev.crashteam.subscription.event.AccountEvent
import org.springframework.stereotype.Component

@Component
class AccountUpdateEventHandler(
    private val accountRepository: AccountRepository
) : AccountEventHandler {

    override fun handle(accountEvents: List<AccountEvent>) {
        for (accountEvent in accountEvents) {
            val accountUpdated = accountEvent.payload.accountUpdated
            val accountEntity = accountRepository.findByUserId(accountEvent.eventSource.userId)!!
            accountEntity.email = accountUpdated.email
            accountEntity.blocked = accountUpdated.enabled
            accountRepository.save(accountEntity)
        }
    }

    override fun isHandle(accountEvent: AccountEvent): Boolean {
        return accountEvent.payload.hasAccountUpdated()
    }
}
