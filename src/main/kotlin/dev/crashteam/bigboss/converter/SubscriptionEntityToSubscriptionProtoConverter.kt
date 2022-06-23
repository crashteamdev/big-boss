package dev.crashteam.bigboss.converter

import dev.crashteam.bigboss.repository.entity.SubscriptionEntity
import dev.crashteam.subscription.Subscription
import org.springframework.stereotype.Component

@Component
class SubscriptionEntityToSubscriptionProtoConverter : DataConverter<SubscriptionEntity, Subscription> {

    override fun convert(source: SubscriptionEntity): Subscription {
        return Subscription.newBuilder()
            .setSubId(source.id.toString())
            .setName(source.name)
            .setDescription(source.description)
            .setPrice(source.price!!)
            .setLevel(source.level!!.toInt())
            .build()
    }
}
