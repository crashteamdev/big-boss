package dev.crashteam.bigboss.listener

import dev.crashteam.bigboss.handler.account.AccountEventHandler
import dev.crashteam.chest.event.WalletCudEvent
import dev.crashteam.subscription.event.AccountEvent
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class AccountEventListener(
    private val accountEventHandlers: List<AccountEventHandler>
) {

    @KafkaListener(
        topics = ["\${bboss.account-cud-topic-name}"],
        autoStartup = "true",
        containerFactory = "accountCudListenerContainerFactory"
    )
    fun receive(
        @Payload messages: List<ByteArray>,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partitions: List<Int>,
        @Header(KafkaHeaders.OFFSET) offsets: List<Long>,
        ack: Acknowledgment
    ) {
        try {
            List(messages.size) { i ->
                log.info { "Received AccountEvent message with partition-offset=${partitions[i].toString() + "-" + offsets[i]}" }
                AccountEvent.parseFrom(messages[i])
            }.groupBy { entry -> accountEventHandlers.find { it.isHandle(entry) } }
                .forEach { (handler, entries) ->
                    handler?.handle(entries)
                }
            ack.acknowledge()
        } catch (e: Exception) {
            log.error(e) { "Exception during handling KE fetch events" }
            throw e
        }

    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

}
