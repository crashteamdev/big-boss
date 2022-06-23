package dev.crashteam.bigboss.saga

import com.google.protobuf.Timestamp
import dev.crashteam.bigboss.repository.AccountRepository
import dev.crashteam.bigboss.repository.AccountSubscriptionRepository
import dev.crashteam.bigboss.repository.SubscriptionRepository
import dev.crashteam.bigboss.repository.SubscriptionSagaCoordinatorRepository
import dev.crashteam.bigboss.repository.entity.AccountSubscriptionState
import dev.crashteam.bigboss.repository.entity.SagaState
import dev.crashteam.bigboss.repository.entity.SubscriptionSagaCoordinatorEntity
import dev.crashteam.bigboss.saga.event.CommitUserSubscriptionEvent
import dev.crashteam.bigboss.saga.event.RollbackUserSubscriptionEvent
import dev.crashteam.bigboss.saga.event.SetUserSubscriptionEvent
import dev.crashteam.chest.event.WalletCommandEvent
import dev.crashteam.chest.event.WalletReserveCredit
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.Instant
import java.util.*

@Service
class SubscriptionSagaCoordinator(
    private val sagaCoordinatorRepository: SubscriptionSagaCoordinatorRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val accountRepository: AccountRepository,
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>
) {

    @Value("\${bboss.wallet-command-topic-name}")
    lateinit var walletCommandTopicName: String

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun setUserSubscription(setUserSubscriptionEvent: SetUserSubscriptionEvent) {
        val accountEntity = accountRepository.findByUserId(setUserSubscriptionEvent.userId)
        val subscriptionEntity =
            subscriptionRepository.findById(UUID.fromString(setUserSubscriptionEvent.subscriptionId)).orElse(null)
        val trxId = UUID.randomUUID().toString()
        val subscriptionSagaCoordinatorEntity = SubscriptionSagaCoordinatorEntity().apply {
            this.trxId = trxId
            this.subscription = subscriptionEntity
            this.account = accountEntity
            this.state = SagaState.in_progress
        }
        sagaCoordinatorRepository.save(subscriptionSagaCoordinatorEntity)
        val walletCommand = WalletCommandEvent.newBuilder().apply {
            this.eventId = UUID.randomUUID().toString()
            val now = Instant.now()
            this.createdAt = Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build()
            this.payload = WalletCommandEvent.EventPayload.newBuilder().apply {
                this.walletReserveCredit = WalletReserveCredit.newBuilder().apply {
                    this.trxId = trxId
                    this.amount = subscriptionEntity.price!!
                    this.userId = setUserSubscriptionEvent.userId
                    this.description = "Set user subscription"
                }.build()
            }.build()
        }.build()
        val producerRecord = ProducerRecord(
            walletCommandTopicName,
            subscriptionSagaCoordinatorEntity.trxId,
            walletCommand.toByteArray()
        )
        kafkaTemplate.send(producerRecord)
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun commitUserSubscription(commitUserSubscriptionEvent: CommitUserSubscriptionEvent) {
        val sagaCoordinatorEntity =
            sagaCoordinatorRepository.findById(commitUserSubscriptionEvent.trxId).orElse(null)
        if (sagaCoordinatorEntity == null) {
            log.warn { "Saga coordinate transaction not found: trxId=${commitUserSubscriptionEvent.trxId}" }
            return
        }
        val accountEntity = sagaCoordinatorEntity.account
        val accountSubscription = accountEntity?.accountSubscription!!
        accountSubscription.state = AccountSubscriptionState.active
        accountSubscriptionRepository.save(accountSubscription)

        sagaCoordinatorEntity.state = SagaState.commit
        sagaCoordinatorRepository.save(sagaCoordinatorEntity)
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun rollbackUserSubscription(rollbackUserSubscriptionEvent: RollbackUserSubscriptionEvent) {
        val sagaCoordinatorEntity =
            sagaCoordinatorRepository.findById(rollbackUserSubscriptionEvent.trxId).orElse(null)
        if (sagaCoordinatorEntity == null) {
            log.warn { "Saga coordinate transaction not found: trxId=${rollbackUserSubscriptionEvent.trxId}" }
            return
        }
        val accountEntity = sagaCoordinatorEntity.account
        accountSubscriptionRepository.deleteById(accountEntity?.accountSubscription?.id!!)

        sagaCoordinatorEntity.state = SagaState.rollback
        sagaCoordinatorRepository.save(sagaCoordinatorEntity)
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

}
