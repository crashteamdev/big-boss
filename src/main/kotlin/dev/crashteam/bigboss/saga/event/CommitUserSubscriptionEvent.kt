package dev.crashteam.bigboss.saga.event

data class CommitUserSubscriptionEvent(
    val trxId: String,
    val userId: String,
    val reservedAmount: Long,
    val balance: Long
)
