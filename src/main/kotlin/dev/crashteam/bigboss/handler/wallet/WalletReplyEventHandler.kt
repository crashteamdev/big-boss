package dev.crashteam.bigboss.handler.wallet

import dev.crashteam.chest.event.WalletReplyEvent

interface WalletReplyEventHandler {
    fun handle(walletEvents: List<WalletReplyEvent>)

    fun isHandle(walletEvent: WalletReplyEvent): Boolean
}
