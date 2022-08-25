package dev.crashteam.bigboss.repository.entity

import javax.persistence.*

@Entity
@Table(name = "account_wallet")
class AccountWalletEntity {
    @Id
    @Column(name = "wallet_id", nullable = false)
    var walletId: String? = null

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    var account: AccountEntity? = null

    @Column(nullable = false)
    var balance: Long? = null

    @Column(nullable = false)
    var blocked: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountWalletEntity

        if (walletId != other.walletId) return false

        return true
    }

    override fun hashCode(): Int {
        return walletId?.hashCode() ?: 0
    }


}
