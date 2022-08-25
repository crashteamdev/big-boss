package dev.crashteam.bigboss.listener

import dev.crashteam.bigboss.handler.wallet.WalletCudEventHandler
import dev.crashteam.chest.event.WalletCudEvent
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class WalletCudEventListener(
    private val walletCudEventHandler: List<WalletCudEventHandler>
) {

    @KafkaListener(
        topics = ["\${bboss.wallet-command-topic-name}"],
        autoStartup = "true",
        containerFactory = "walletCommandListenerContainerFactory"
    )
    fun receive(
        @Payload messages: List<ByteArray>,
        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partitions: List<Int>,
        @Header(KafkaHeaders.OFFSET) offsets: List<Long>,
        ack: Acknowledgment
    ) {
        try {
            List(messages.size) { i ->
                log.info { "Received WalletCudEvent message with partition-offset=${partitions[i].toString() + "-" + offsets[i]}" }
                WalletCudEvent.parseFrom(messages[i])
            }.groupBy { entry -> walletCudEventHandler.find { it.isHandle(entry) } }
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
