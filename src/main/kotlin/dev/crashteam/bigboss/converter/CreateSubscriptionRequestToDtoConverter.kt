package dev.crashteam.bigboss.converter

import dev.crashteam.bigboss.service.model.CreateSubscriptionDto
import dev.crashteam.subscription.CreateSubscriptionRequest
import org.springframework.stereotype.Component

@Component
class CreateSubscriptionRequestToDtoConverter : DataConverter<CreateSubscriptionRequest, CreateSubscriptionDto> {

    override fun convert(source: CreateSubscriptionRequest): CreateSubscriptionDto {
        return CreateSubscriptionDto(
            productId = source.productId,
            name = source.name,
            description = source.description,
            price = source.price,
            level = source.level.toShort()
        )
    }
}
