package dev.crashteam.bigboss.repository

import dev.crashteam.bigboss.repository.entity.SubscriptionSagaCoordinatorEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionSagaCoordinatorRepository : JpaRepository<SubscriptionSagaCoordinatorEntity, String>
