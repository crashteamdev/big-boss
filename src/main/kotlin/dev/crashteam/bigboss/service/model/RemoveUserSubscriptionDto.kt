package dev.crashteam.bigboss.service.model

import java.util.*

data class RemoveUserSubscriptionDto(
    val userId: String,
    val subscriptionId: UUID
)
