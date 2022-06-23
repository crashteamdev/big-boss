package dev.crashteam.bigboss.handler.wallet

import dev.crashteam.bigboss.saga.event.CommitUserSubscriptionEvent
import dev.crashteam.chest.event.WalletReplyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class WalletCreditReservedEventHandler(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : WalletReplyEventHandler {

    @Transactional
    override fun handle(walletEvents: List<WalletReplyEvent>) {
        for (walletEvent in walletEvents) {
            val walletCreditReserved = walletEvent.payload.walletCreditReserved
            applicationEventPublisher.publishEvent(
                CommitUserSubscriptionEvent(
                    trxId = walletCreditReserved.trxId,
                    userId = walletCreditReserved.userId,
                    reservedAmount = walletCreditReserved.reservedAmount,
                    balance = walletCreditReserved.balance
                )
            )
        }
    }

    override fun isHandle(walletEvent: WalletReplyEvent): Boolean {
        return walletEvent.payload.hasWalletCreditReserved()
    }
}
