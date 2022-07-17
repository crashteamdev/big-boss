package dev.crashteam.bigboss.service

import dev.crashteam.bigboss.AbstractIntegrationTest
import dev.crashteam.bigboss.repository.ProductRepository
import dev.crashteam.bigboss.repository.entity.ProductEntity
import dev.crashteam.bigboss.service.model.CreateSubscriptionDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class SubscriptionServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var subscriptionService: SubscriptionService

    @Autowired
    lateinit var productRepository: ProductRepository

    val productId = UUID.randomUUID()

    @BeforeEach
    internal fun setUp() {
        val productEntity = ProductEntity().apply {
            this.id = productId
            this.name = "analytics"
            this.description = "just best of the best analytic"
        }
        productRepository.save(productEntity)
    }

    @Test
    fun `create subscription test`() {
        // Given
        val createSubscriptionDto =
            CreateSubscriptionDto(productId.toString(), "testSub", "test subscription", 10000, 1)

        // When
        val createdSubscription = subscriptionService.createSubscription(createSubscriptionDto)

        // Then
        Assertions.assertEquals(createSubscriptionDto.name, createdSubscription.name)
        Assertions.assertEquals(createSubscriptionDto.description, createdSubscription.description)
        Assertions.assertEquals(createSubscriptionDto.level, createdSubscription.level)
        Assertions.assertEquals(createSubscriptionDto.price, createdSubscription.price)
    }

}
