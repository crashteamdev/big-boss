package dev.crashteam.bigboss.listener

import dev.crashteam.bigboss.handler.wallet.WalletReplyEventHandler
import dev.crashteam.chest.event.WalletCudEvent
import dev.crashteam.chest.event.WalletReplyEvent
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class WalletReplyEventListener(
    private val walletReplyEventHandler: List<WalletReplyEventHandler>
) {

    @KafkaListener(
        topics = ["\${bboss.wallet-command-response-topic-name}"],
        autoStartup = "true",
        containerFactory = "walletReplyCommandListenerContainerFactory"
    )
    fun receive(
        @Payload messages: List<ByteArray>,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partitions: List<Int>,
        @Header(KafkaHeaders.OFFSET) offsets: List<Long>,
        ack: Acknowledgment
    ) {
        try {
            List(messages.size) { i ->
                log.info { "Received WalletReplyEvent message with partition-offset=${partitions[i].toString() + "-" + offsets[i]}" }
                WalletReplyEvent.parseFrom(messages[i])
            }.groupBy { entry -> walletReplyEventHandler.find { it.isHandle(entry) } }
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
