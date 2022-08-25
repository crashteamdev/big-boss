package dev.crashteam.bigboss.handler.account

import dev.crashteam.subscription.event.AccountEvent

interface AccountEventHandler {
    fun handle(accountEvents: List<AccountEvent>)

    fun isHandle(accountEvent: AccountEvent): Boolean
}
