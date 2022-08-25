package dev.crashteam.bigboss.service.model

import java.time.LocalDateTime
import java.util.*

data class AddUserSubscriptionDto(
    val userId: String,
    val productId: UUID,
    val subscriptionId: UUID,
    val period: Int
)
