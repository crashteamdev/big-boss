package dev.crashteam.bigboss.saga.event

data class RollbackUserSubscriptionEvent(
    val trxId: String,
    val userId: String,
)
