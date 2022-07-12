package dev.crashteam.bigboss.repository.entity

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "product",  uniqueConstraints = [
    UniqueConstraint(name = "product_name_idx", columnNames = ["name"])
])
class ProductEntity {
    @Id
    var id: UUID? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var description: String? = null

    @OneToMany(mappedBy="product", fetch = FetchType.LAZY)
    var subscriptions: Set<SubscriptionEntity>? = null

}
