package dev.crashteam.bigboss.saga.event

data class SetUserSubscriptionEvent(
    val userId: String,
    val subscriptionId: String,
    val period: Int
)
