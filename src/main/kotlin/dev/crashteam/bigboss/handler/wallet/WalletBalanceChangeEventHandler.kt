package dev.crashteam.bigboss.handler.wallet

import dev.crashteam.bigboss.repository.AccountWalletRepository
import dev.crashteam.chest.event.WalletBalanceChange
import dev.crashteam.chest.event.WalletCudEvent
import org.springframework.stereotype.Component

@Component
class WalletBalanceChangeEventHandler(
    private val walletRepository: AccountWalletRepository
) : WalletCudEventHandler {

    override fun handle(walletEvents: List<WalletCudEvent>) {
        for (walletEvent in walletEvents) {
            val walletBalanceChange = walletEvent.payload.walletChange.walletBalanceChange
            val accountWalletEntity = walletRepository.findById(walletEvent.eventSource.walletId).orElse(null)
            if (walletBalanceChange.type == WalletBalanceChange.BalanceChangeType.replenishment) {
                accountWalletEntity.balance = accountWalletEntity.balance!! + walletBalanceChange.amount
            } else if (walletBalanceChange.type == WalletBalanceChange.BalanceChangeType.withdrawal) {
                accountWalletEntity.balance = accountWalletEntity.balance!! - walletBalanceChange.amount
            }
            walletRepository.save(accountWalletEntity)
        }
    }

    override fun isHandle(walletEvent: WalletCudEvent): Boolean {
        return walletEvent.payload.hasWalletChange() && walletEvent.payload.walletChange.hasWalletBalanceChange()
    }
}
