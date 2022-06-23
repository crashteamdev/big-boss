package dev.crashteam.bigboss.service

import com.google.protobuf.Int32Value
import com.google.protobuf.Int64Value
import com.google.protobuf.StringValue
import com.google.protobuf.Timestamp
import dev.crashteam.bigboss.repository.SubscriptionRepository
import dev.crashteam.bigboss.repository.entity.SubscriptionEntity
import dev.crashteam.bigboss.service.model.CreateSubscriptionDto
import dev.crashteam.bigboss.service.model.ModifySubscriptionDto
import dev.crashteam.subscription.event.SubscriptionChange
import dev.crashteam.subscription.event.SubscriptionCreated
import dev.crashteam.subscription.event.SubscriptionEvent
import dev.crashteam.subscription.event.SubscriptionModify
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.transaction.Transactional

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>
) {

    @Transactional
    fun createSubscription(subscriptionDto: CreateSubscriptionDto): SubscriptionEntity {
        val subscriptionEntity = SubscriptionEntity().apply {
            id = UUID.randomUUID()
            name = subscriptionDto.name
            description = subscriptionDto.description
            price = subscriptionDto.price
            level = subscriptionDto.level
        }
        val savedSubscription = subscriptionRepository.save(subscriptionEntity)
//        val now = Instant.now()
//        val subscriptionEvent = SubscriptionEvent.newBuilder()
//            .setCreatedAt(Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build())
//            .setEventSource(
//                SubscriptionEvent.EventSource.newBuilder().setSubId(savedSubscription.id.toString()).build()
//            )
//            .setPayload(
//                SubscriptionEvent.EventPayload.newBuilder()
//                    .setSubscriptionChange(
//                        SubscriptionChange.newBuilder()
//                            .setSubscriptionCreated(
//                                SubscriptionCreated.newBuilder()
//                                    .setName(savedSubscription.name)
//                                    .setDescription(savedSubscription.description)
//                                    .setLevel(savedSubscription.level!!.toInt())
//                                    .setPrice(savedSubscription.price!!)
//                                    .build()
//                            )
//                            .build()
//                    )
//                    .build()
//            )
//            .build()
//        publishSubscriptionEvent(subscriptionEntity.id.toString(), subscriptionEvent)

        return savedSubscription
    }

    @Transactional
    fun modifySubscription(modifySubscriptionDto: ModifySubscriptionDto): SubscriptionEntity? {
        val subscriptionEntity = subscriptionRepository.findById(modifySubscriptionDto.subId).orElse(null)
            ?: return null
        if (modifySubscriptionDto.level != null) {
            subscriptionEntity.level = modifySubscriptionDto.level
        }
        if (modifySubscriptionDto.name != null) {
            subscriptionEntity.name = modifySubscriptionDto.name
        }
        if (modifySubscriptionDto.price != null) {
            subscriptionEntity.price = modifySubscriptionDto.price
        }
        if (modifySubscriptionDto.description != null) {
            subscriptionEntity.description = modifySubscriptionDto.description
        }
        val savedSubscription = subscriptionRepository.save(subscriptionEntity)
//        val eventPayload = SubscriptionEvent.EventPayload.newBuilder().apply {
//            this.subscriptionChange = SubscriptionChange.newBuilder().apply {
//                this.subscriptionModify = SubscriptionModify.newBuilder().apply {
//                    if (modifySubscriptionDto.name != null) {
//                        this.name = StringValue.of(savedSubscription.name)
//                    }
//                    if (modifySubscriptionDto.description != null) {
//                        this.description = StringValue.of(savedSubscription.description)
//                    }
//                    if (modifySubscriptionDto.level != null) {
//                        this.level = Int32Value.of(savedSubscription.level!!.toInt())
//                    }
//                    if (modifySubscriptionDto.price != null) {
//                        this.price = Int64Value.of(savedSubscription.price!!)
//                    }
//                }.build()
//            }.build()
//        }.build()
//        val subscriptionEvent = buildSubscriptionEvent(savedSubscription.id.toString(), eventPayload)
//        publishSubscriptionEvent(subscriptionEntity.id.toString(), subscriptionEvent)

        return savedSubscription
    }

//    fun publishSubscriptionEvent(id: String, subscriptionEvent: SubscriptionEvent) {
//        val producerRecord =
//            ProducerRecord(bigBossProperties.subscriptionTopicName, id, subscriptionEvent.toByteArray())
//        log.info { "Publish subscription event: $producerRecord" }
//        kafkaTemplate.send(producerRecord)
//    }

    private fun buildSubscriptionEvent(
        subId: String,
        payload: SubscriptionEvent.EventPayload
    ): SubscriptionEvent {
        val now = Instant.now()
        return SubscriptionEvent.newBuilder()
            .setCreatedAt(Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build())
            .setEventSource(
                SubscriptionEvent.EventSource.newBuilder().setSubId(subId).build()
            )
            .setPayload(payload)
            .build()
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

}
