package dev.crashteam.bigboss.service

import com.google.protobuf.Timestamp
import dev.crashteam.bigboss.AbstractIntegrationTest
import dev.crashteam.bigboss.extensions.KafkaContainerExtension
import dev.crashteam.bigboss.repository.*
import dev.crashteam.bigboss.repository.entity.*
import dev.crashteam.bigboss.service.error.AccountBalanceLimitException
import dev.crashteam.bigboss.service.model.RemoveUserSubscriptionDto
import dev.crashteam.bigboss.service.model.SetUserSubscriptionDto
import dev.crashteam.chest.event.WalletCreditReserved
import dev.crashteam.chest.event.WalletReplyEvent
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

@Import(UserSubscriptionServiceTest.KafkaProducerConfig::class)
class UserSubscriptionServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var subscriptionRepository: SubscriptionRepository

    @Autowired
    lateinit var accountSubscriptionRepository: AccountSubscriptionRepository

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var accountWalletRepository: AccountWalletRepository

    @Autowired
    lateinit var sagaCoordinatorRepository: SubscriptionSagaCoordinatorRepository

    @Autowired
    lateinit var userSubscriptionService: UserSubscriptionService

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, ByteArray>

    @Value("\${bboss.wallet-command-response-topic-name}")
    lateinit var walletCommandResponseTopic: String

    val subscriptionId: UUID = UUID.randomUUID()

    val userId: UUID = UUID.randomUUID()

    @BeforeEach
    internal fun setUp() {
        accountSubscriptionRepository.deleteAll()
        sagaCoordinatorRepository.deleteAll()
        subscriptionRepository.deleteAll()
        accountWalletRepository.deleteAll()
        accountRepository.deleteAll()
        val newSubscriptionEntity = SubscriptionEntity().apply {
            this.id = this@UserSubscriptionServiceTest.subscriptionId
            this.name = "testSubName"
            this.description = "test description"
            this.level = 0
            this.price = 1000
        }
        val accountEntity = AccountEntity().apply {
            this.userId = this@UserSubscriptionServiceTest.userId.toString()
            this.email = "testMail"
        }
        subscriptionRepository.save(newSubscriptionEntity)
        accountRepository.save(accountEntity)
    }

    @Test
    fun `set user subscription with zero balance`() {
        // Given
        val setUserSubscriptionDto = SetUserSubscriptionDto(
            userId = userId.toString(),
            subId = subscriptionId,
            validUntil = LocalDateTime.now().plusDays(30)
        )

        // When
        assertThrows(
            AccountBalanceLimitException::class.java
        ) { userSubscriptionService.setUserSubscription(setUserSubscriptionDto) }
    }

    @Test
    fun `set user subscription`() {
        // Given
        val setUserSubscriptionDto = SetUserSubscriptionDto(
            userId = userId.toString(),
            subId = subscriptionId,
            validUntil = LocalDateTime.now().plusDays(30)
        )

        // When
        val accountEntity = accountRepository.findByUserId(userId.toString())
        val accountWalletEntity = AccountWalletEntity().apply {
            this.walletId = UUID.randomUUID().toString()
            this.account = accountEntity
            this.balance = 100000
            this.blocked = false
        }
        accountWalletRepository.save(accountWalletEntity)
        userSubscriptionService.setUserSubscription(setUserSubscriptionDto)
        val sagaCoordinatorEntity = sagaCoordinatorRepository.findAll().first()
        val walletReplyEvent = WalletReplyEvent.newBuilder().apply {
            this.eventId = UUID.randomUUID().toString()
            this.eventSource = WalletReplyEvent.EventSource.newBuilder().apply {
                this.walletId = accountWalletEntity.walletId
            }.build()
            val now = Instant.now()
            this.createdAt = Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build()
            this.payload = WalletReplyEvent.EventPayload.newBuilder().apply {
                this.walletCreditReserved = WalletCreditReserved.newBuilder().apply {
                    this.userId = accountEntity!!.userId
                    this.trxId = sagaCoordinatorEntity.trxId
                }.build()
            }.build()
        }.build()
        Thread.sleep(2000)
        kafkaTemplate.send(
            ProducerRecord(
                walletCommandResponseTopic,
                sagaCoordinatorEntity.trxId,
                walletReplyEvent.toByteArray()
            )
        )
        await.atMost(60, TimeUnit.SECONDS).until {
            accountRepository.findByUserId(userId.toString())?.accountSubscription?.state == AccountSubscriptionState.active
        }
        val account = accountRepository.findByUserId(userId.toString())

        // Then
        assertTrue(account?.accountSubscription?.state == AccountSubscriptionState.active)
    }

    @Test
    fun `remove user subscription`() {
        // Given
        val accountEntity = accountRepository.findByUserId(userId.toString())
        val subscriptionEntity = subscriptionRepository.findById(subscriptionId).orElse(null)
        val accountSubscriptionEntity = AccountSubscriptionEntity().apply {
            this.account = accountEntity
            this.subscription = subscriptionEntity
            this.state = AccountSubscriptionState.active
            this.validUntil = LocalDateTime.now().plusDays(30)
        }

        // When
        accountSubscriptionRepository.save(accountSubscriptionEntity)
        userSubscriptionService.removeUserSubscription(
            RemoveUserSubscriptionDto(
                userId.toString(),
                subscriptionEntity.id!!
            )
        )
        val accountWithoutSub = accountRepository.findByUserId(userId.toString())

        // Then
        assertTrue(accountWithoutSub?.accountSubscription == null)
    }

    @TestConfiguration
    class KafkaProducerConfig {

        @Value("\${bboss.wallet-command-topic-name}")
        lateinit var walletCommandTopic: String

        @Value("\${bboss.wallet-command-response-topic-name}")
        lateinit var walletCommandResponseTopic: String

        fun producerFactory(): ProducerFactory<String, ByteArray> {
            val configProps: MutableMap<String, Any> = HashMap()
            configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = KafkaContainerExtension.kafka.bootstrapServers
            configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
            configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java
            return DefaultKafkaProducerFactory(configProps)
        }

        @Bean
        fun paymentKafkaTemplate(): KafkaTemplate<String, ByteArray> {
            AdminClient.create(
                mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to KafkaContainerExtension.kafka.bootstrapServers)
            ).use {
                it.createTopics(
                    listOf(
                        NewTopic(walletCommandTopic, 1, 1),
                        NewTopic(walletCommandResponseTopic, 1, 1)
                    )
                )
            }
            return KafkaTemplate(producerFactory())
        }
    }

}
