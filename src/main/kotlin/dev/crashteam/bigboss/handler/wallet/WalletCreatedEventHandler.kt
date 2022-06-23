package dev.crashteam.bigboss.handler.wallet

import dev.crashteam.bigboss.repository.AccountRepository
import dev.crashteam.bigboss.repository.AccountWalletRepository
import dev.crashteam.bigboss.repository.entity.AccountWalletEntity
import dev.crashteam.chest.event.WalletCudEvent
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class WalletCreatedEventHandler(
    private val accountRepository: AccountRepository,
    private val accountWalletRepository: AccountWalletRepository
) : WalletCudEventHandler {

    @Transactional
    override fun handle(walletEvents: List<WalletCudEvent>) {
        for (walletEvent in walletEvents) {
            val walletCreated = walletEvent.payload.walletChange.walletCreated
            val accountEntity = accountRepository.findByUserId(walletEvent.payload.walletChange.userId)
            val accountWalletEntity = AccountWalletEntity().apply {
                this.walletId = walletEvent.eventSource.walletId
                this.account = accountEntity
                this.balance = walletCreated.balance
            }
            accountWalletRepository.save(accountWalletEntity)
        }
    }

    override fun isHandle(walletEvent: WalletCudEvent): Boolean {
        return walletEvent.payload.hasWalletChange() && walletEvent.payload.walletChange.hasWalletCreated()
    }
}
