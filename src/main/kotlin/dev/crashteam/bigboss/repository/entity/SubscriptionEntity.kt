package dev.crashteam.bigboss.repository.entity

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "subscription",  uniqueConstraints = [
    UniqueConstraint(name = "subscription_name_idx", columnNames = ["name"])
])
class SubscriptionEntity {
    @Id
    var id: UUID? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var description: String? = null

    @Column(nullable = false)
    var price: Long? = null

    @Column(nullable = false, unique = true)
    var level: Short? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubscriptionEntity

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}
