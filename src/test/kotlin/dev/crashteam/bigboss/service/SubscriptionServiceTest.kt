package dev.crashteam.bigboss.service

import dev.crashteam.bigboss.AbstractIntegrationTest
import dev.crashteam.bigboss.service.model.CreateSubscriptionDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubscriptionServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var subscriptionService: SubscriptionService

    @Test
    fun `create subscription test`() {
        // Given
        val createSubscriptionDto = CreateSubscriptionDto("testSub", "test subscription", 10000, 1)

        // When
        val createdSubscription = subscriptionService.createSubscription(createSubscriptionDto)

        // Then
        Assertions.assertEquals(createSubscriptionDto.name, createdSubscription.name)
        Assertions.assertEquals(createSubscriptionDto.description, createdSubscription.description)
        Assertions.assertEquals(createSubscriptionDto.level, createdSubscription.level)
        Assertions.assertEquals(createSubscriptionDto.price, createdSubscription.price)
    }

}
