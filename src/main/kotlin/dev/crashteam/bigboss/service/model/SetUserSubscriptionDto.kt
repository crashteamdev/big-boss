package dev.crashteam.bigboss.service.model

import java.time.LocalDateTime
import java.util.*

data class SetUserSubscriptionDto(
    val userId: String,
    val subId: UUID,
    val validUntil: LocalDateTime
)
