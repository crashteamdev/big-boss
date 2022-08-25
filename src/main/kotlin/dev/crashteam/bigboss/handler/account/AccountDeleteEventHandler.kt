//package dev.crashteam.bigboss.handler.account
//
//import dev.crashteam.bigboss.repository.AccountRepository
//import dev.crashteam.subscription.event.AccountEvent
//import org.springframework.stereotype.Component
//
//@Component
//class AccountDeleteEventHandler(
//    private val accountRepository: AccountRepository
//) : AccountEventHandler {
//
//    override fun handle(accountEvents: List<AccountEvent>) {
//        for (accountEvent in accountEvents) {
//            val accountDeleted = accountEvent.payload.accountDeleted
//            accountRepository.deleteByUserId(accountEvent.eventSource.userId)
//        }
//    }
//
//    override fun isHandle(accountEvent: AccountEvent): Boolean {
//        return accountEvent.payload.hasAccountDeleted()
//    }
//}
