package dev.crashteam.bigboss.saga.event

data class UpgradeUserSubscriptionEvent(
    val userId: String,
    val fromSubId: String,
    val toSubId: String
)
