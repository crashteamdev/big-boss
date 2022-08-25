package dev.crashteam.bigboss.handler.wallet

import dev.crashteam.bigboss.repository.AccountWalletRepository
import dev.crashteam.chest.event.WalletBlockChange
import dev.crashteam.chest.event.WalletCudEvent
import org.springframework.stereotype.Component

@Component
class WalletBlockChangeEventHandler(
    private val walletRepository: AccountWalletRepository
) : WalletCudEventHandler {

    override fun handle(walletEvents: List<WalletCudEvent>) {
        for (walletEvent in walletEvents) {
            val walletBlocked = walletEvent.payload.walletChange.walletBlocked
            val accountWalletEntity = walletRepository.findById(walletEvent.eventSource.walletId).orElse(null)
            accountWalletEntity.blocked = when (walletBlocked.type) {
                WalletBlockChange.BlockType.BLOCKED -> {
                    true
                }
                WalletBlockChange.BlockType.UNBLOCKED -> {
                    false
                }
                else -> false
            }
            walletRepository.save(accountWalletEntity)
        }
    }

    override fun isHandle(walletEvent: WalletCudEvent): Boolean {
        return walletEvent.payload.hasWalletChange() && walletEvent.payload.walletChange.hasWalletBlocked()
    }
}
