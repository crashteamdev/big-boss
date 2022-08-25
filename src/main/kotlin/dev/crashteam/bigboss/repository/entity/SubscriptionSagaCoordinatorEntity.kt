package dev.crashteam.bigboss.repository.entity

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.*

@Entity
@Table(name = "subscription_saga_coordinator")
@TypeDef(
    name = "pgsql_enum",
    typeClass = PostgreSQLEnumType::class
)
class SubscriptionSagaCoordinatorEntity {
    @Id
    var trxId: String? = null

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    var account: AccountEntity? = null

    @Enumerated(EnumType.STRING)
    @Type(type = "pgsql_enum")
    @Column(nullable = false)
    var state: SagaState? = null

    @OneToOne
    @JoinColumn(name = "subscription_id", referencedColumnName = "id")
    var subscription: SubscriptionEntity? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubscriptionSagaCoordinatorEntity

        if (trxId != other.trxId) return false

        return true
    }

    override fun hashCode(): Int {
        return trxId?.hashCode() ?: 0
    }


}
