package dev.crashteam.bigboss.handler.wallet

import dev.crashteam.bigboss.saga.event.RollbackUserSubscriptionEvent
import dev.crashteam.chest.event.WalletReplyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class WalletUserNotFoundEventHandler(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : WalletReplyEventHandler {

    override fun handle(walletEvents: List<WalletReplyEvent>) {
        for (walletEvent in walletEvents) {
            val walletCreditLimitExceeded = walletEvent.payload.walletUserNotFound
            applicationEventPublisher.publishEvent(
                RollbackUserSubscriptionEvent(
                    trxId = walletCreditLimitExceeded.trxId,
                    userId = walletCreditLimitExceeded.userId,
                )
            )
        }
    }

    override fun isHandle(walletEvent: WalletReplyEvent): Boolean {
        return walletEvent.payload.hasWalletUserNotFound()
    }
}
