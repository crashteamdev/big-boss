package dev.crashteam.bigboss.repository

import dev.crashteam.bigboss.repository.entity.AccountSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AccountSubscriptionRepository : JpaRepository<AccountSubscriptionEntity, Long> {

    fun findBySubscriptionId(subscriptionId: UUID): AccountSubscriptionEntity?

    fun findByAccount_UserId(userId: String): AccountSubscriptionEntity?

    fun deleteByAccount_UserIdAndSubscription_Id(userId: String, subscriptionId: UUID)
}
