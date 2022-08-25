package dev.crashteam.bigboss.service.model

data class CreateSubscriptionDto(
    val productId: String,
    val name: String,
    val description: String,
    val price: Long,
    val level: Short,
)
