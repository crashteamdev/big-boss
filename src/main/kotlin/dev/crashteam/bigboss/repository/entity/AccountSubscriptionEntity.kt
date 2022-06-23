package dev.crashteam.bigboss.repository.entity

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "account_subscription")
@TypeDef(
    name = "pgsql_enum",
    typeClass = PostgreSQLEnumType::class
)
class AccountSubscriptionEntity : BaseEntity<Long>() {

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    var account: AccountEntity? = null

    @OneToOne
    @JoinColumn(name = "subscription_id", referencedColumnName = "id")
    var subscription: SubscriptionEntity? = null

    @Column(nullable = false)
    var validUntil: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(nullable = false)
    var state: AccountSubscriptionState? = null
}
