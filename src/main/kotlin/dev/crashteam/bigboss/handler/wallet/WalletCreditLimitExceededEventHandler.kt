package dev.crashteam.bigboss.handler.wallet

import dev.crashteam.bigboss.repository.SubscriptionSagaCoordinatorRepository
import dev.crashteam.bigboss.saga.event.RollbackUserSubscriptionEvent
import dev.crashteam.chest.event.WalletReplyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class WalletCreditLimitExceededEventHandler(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val sagaCoordinatorRepository: SubscriptionSagaCoordinatorRepository,
) : WalletReplyEventHandler {

    @Transactional
    override fun handle(walletEvents: List<WalletReplyEvent>) {
        for (walletEvent in walletEvents) {
            val walletCreditLimitExceeded = walletEvent.payload.walletCreditLimitExceeded
            applicationEventPublisher.publishEvent(
                RollbackUserSubscriptionEvent(
                    trxId = walletCreditLimitExceeded.trxId,
                    userId = walletCreditLimitExceeded.userId,
                )
            )
        }
    }

    override fun isHandle(walletEvent: WalletReplyEvent): Boolean {
        return walletEvent.payload.hasWalletCreditLimitExceeded() &&
                sagaCoordinatorRepository.findById(walletEvent.payload.walletCreditLimitExceeded.trxId)
                    .orElse(null) != null
    }
}
