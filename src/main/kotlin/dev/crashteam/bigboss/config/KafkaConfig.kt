package dev.crashteam.bigboss.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    lateinit var bootstrapServers: String

    @Bean
    fun producerConfigs(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java
        props[ProducerConfig.BATCH_SIZE_CONFIG] = 50
        return props
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, ByteArray> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, ByteArray> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun walletCommandConsumerConfigs(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
        props[ConsumerConfig.GROUP_ID_CONFIG] = "bigboss-wallet-command-group"
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "50"
        return props
    }

    @Bean
    fun walletCommandListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ByteArray> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, ByteArray> =
            ConcurrentKafkaListenerContainerFactory()
        factory.consumerFactory = DefaultKafkaConsumerFactory(walletCommandConsumerConfigs())
        factory.isBatchListener = true
        factory.setCommonErrorHandler(DefaultErrorHandler(FixedBackOff()).apply { isAckAfterHandle = false })
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        return factory
    }

    @Bean
    fun walletReplyCommandConsumerConfigs(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java
        props[ConsumerConfig.GROUP_ID_CONFIG] = "bigboss-wallet-reply-command-group"
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "50"
        return props
    }

    @Bean
    fun walletReplyCommandListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ByteArray> {
        val factory: ConcurrentKafkaListenerContainerFactory<String, ByteArray> =
            ConcurrentKafkaListenerContainerFactory()
        factory.consumerFactory = DefaultKafkaConsumerFactory(walletReplyCommandConsumerConfigs())
        factory.isBatchListener = true
        factory.setCommonErrorHandler(DefaultErrorHandler(FixedBackOff()).apply { isAckAfterHandle = false })
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        return factory
    }

}
