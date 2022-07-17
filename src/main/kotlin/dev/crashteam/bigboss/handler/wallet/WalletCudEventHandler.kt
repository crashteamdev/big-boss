package dev.crashteam.bigboss.handler.wallet

import dev.crashteam.chest.event.WalletCudEvent

interface WalletCudEventHandler {
    fun handle(walletEvents: List<WalletCudEvent>)

    fun isHandle(walletEvent: WalletCudEvent): Boolean
}
