package dev.crashteam.bigboss.converter

import dev.crashteam.bigboss.service.model.ModifySubscriptionDto
import dev.crashteam.subscription.ModifySubscriptionRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class ModifySubscriptionRequestToDtoConverter : DataConverter<ModifySubscriptionRequest, ModifySubscriptionDto> {

    override fun convert(source: ModifySubscriptionRequest): ModifySubscriptionDto {
        return ModifySubscriptionDto(
            subId = UUID.fromString(source.subId),
            name = if (source.modifyField.hasName()) source.modifyField.name.value else null,
            description = if (source.modifyField.hasDescription()) source.modifyField.description.value else null,
            price = if (source.modifyField.hasPrice()) source.modifyField.price.value else null,
            level = if (source.modifyField.hasLevel()) source.modifyField.level.value.toShort() else null
        )
    }
}
