package dev.crashteam.bigboss.converter

import dev.crashteam.bigboss.service.model.RemoveUserSubscriptionDto
import dev.crashteam.subscription.RemoveAccountSubscriptionRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class RemoveUserSubscriptionToDtoConverter : DataConverter<RemoveAccountSubscriptionRequest, RemoveUserSubscriptionDto> {

    override fun convert(source: RemoveAccountSubscriptionRequest): RemoveUserSubscriptionDto? {
        return RemoveUserSubscriptionDto(
            userId = source.userId,
            subId = UUID.fromString(source.subId)
        )
    }
}
