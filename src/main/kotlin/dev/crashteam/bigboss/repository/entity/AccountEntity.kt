package dev.crashteam.bigboss.repository.entity

import javax.persistence.*

@Entity
@Table(name = "account")
class AccountEntity : BaseEntity<Long>() {
    @Column(nullable = false)
    var userId: String? = null

    @Column(nullable = false)
    var email: String? = null

    @Column(nullable = false)
    var blocked: Boolean = false

    @OneToMany(mappedBy="account", fetch = FetchType.EAGER)
    var accountSubscriptions: Set<AccountSubscriptionEntity>? = null

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    var wallet: AccountWalletEntity? = null

}
