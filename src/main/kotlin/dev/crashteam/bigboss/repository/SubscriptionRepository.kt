package dev.crashteam.bigboss.repository

import dev.crashteam.bigboss.repository.entity.SubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SubscriptionRepository : JpaRepository<SubscriptionEntity, UUID> {
    fun findByIdAndProductId(subscriptionId: UUID, productId: UUID): SubscriptionEntity?

    fun findByProduct_Id(productId: UUID): List<SubscriptionEntity>
}
