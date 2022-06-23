package dev.crashteam.bigboss.service.model

import java.util.*

data class ModifySubscriptionDto(
    val subId: UUID,
    val name: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val level: Short? = null
)
