package dev.crashteam.bigboss.service

import dev.crashteam.bigboss.repository.AccountRepository
import dev.crashteam.bigboss.repository.AccountSubscriptionRepository
import dev.crashteam.bigboss.repository.SubscriptionRepository
import dev.crashteam.bigboss.repository.entity.AccountEntity
import dev.crashteam.bigboss.repository.entity.AccountSubscriptionEntity
import dev.crashteam.bigboss.repository.entity.AccountSubscriptionState
import dev.crashteam.bigboss.saga.event.SetUserSubscriptionEvent
import dev.crashteam.bigboss.service.error.AccountBalanceLimitException
import dev.crashteam.bigboss.service.error.SubscriptionAlreadyExistsException
import dev.crashteam.bigboss.service.error.SubscriptionNotFoundException
import dev.crashteam.bigboss.service.model.AddUserSubscriptionDto
import dev.crashteam.bigboss.service.model.RemoveUserSubscriptionDto
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class UserSubscriptionService(
    private val accountRepository: AccountRepository,
    private val accountSubscriptionRepository: AccountSubscriptionRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun addUserSubscription(addUserSubscriptionDto: AddUserSubscriptionDto): AccountSubscriptionEntity {
        val accountEntity: AccountEntity = accountRepository.findByUserId(addUserSubscriptionDto.userId)!!
        val subscriptionEntity = subscriptionRepository.findByIdAndProductId(
            addUserSubscriptionDto.subscriptionId,
            addUserSubscriptionDto.productId
        )
            ?: throw SubscriptionNotFoundException("Not found by subscriptionId=${addUserSubscriptionDto.subscriptionId}" +
                    " and productId=${addUserSubscriptionDto.productId}")
        val accountSubscription =
            accountEntity.accountSubscriptions?.find { it.subscription?.id == addUserSubscriptionDto.subscriptionId }
        if (accountSubscription?.validUntil != null) {
            if (accountSubscription.validUntil!! < LocalDateTime.now()) {
                throw SubscriptionAlreadyExistsException("Subscription already exists. Wait until it expire or try to upgrade")
            }
        }
        val accBalance = accountEntity.wallet?.balance ?: 0
        val subscriptionPrice = subscriptionEntity.price!! * addUserSubscriptionDto.period
        if (accBalance < subscriptionPrice) {
            throw AccountBalanceLimitException("Not enough account balance. balance=${accBalance}; price=${subscriptionEntity.price}")
        }
        val validUntil = LocalDateTime.now().plusDays(30L * addUserSubscriptionDto.period)
        val accountSubscriptionEntity = if (accountSubscription == null) {
            val accountSubscriptionEntity = AccountSubscriptionEntity().apply {
                this.account = accountEntity
                this.subscription = subscriptionEntity
                this.validUntil = validUntil
                this.state = AccountSubscriptionState.suspended
            }
            accountSubscriptionRepository.save(accountSubscriptionEntity)
        } else {
            accountSubscription.subscription = subscriptionEntity
            accountSubscription.validUntil = validUntil
            accountSubscription.state = AccountSubscriptionState.suspended
            accountSubscriptionRepository.save(accountSubscription)
        }
        applicationEventPublisher.publishEvent(
            SetUserSubscriptionEvent(
                addUserSubscriptionDto.userId,
                addUserSubscriptionDto.subscriptionId.toString(),
                addUserSubscriptionDto.period
            )
        )

        return accountSubscriptionEntity
    }

    @Transactional
    fun removeUserSubscription(removeUserSubscriptionDto: RemoveUserSubscriptionDto) {
        accountSubscriptionRepository.deleteByAccount_UserIdAndSubscription_Id(
            removeUserSubscriptionDto.userId,
            removeUserSubscriptionDto.subscriptionId
        )
    }

    @Transactional
    fun getAccountSubscriptions(userId: String): Set<AccountSubscriptionEntity>? {
        return accountRepository.findByUserId(userId)?.accountSubscriptions
    }

//    fun upgradeUserSubscription(userId: String, fromSubId: UUID, toSubId: UUID) {
//        val fromSubscription = subscriptionRepository.findById(fromSubId).orElse(null)
//        val toSubscription = subscriptionRepository.findById(toSubId).orElse(null)
//        if (fromSubscription == null || toSubscription == null) {
//            throw SubscriptionNotFoundException("Subscription not found. from=${fromSubscription.id}; to=${toSubscription.id}")
//        }
//        if (toSubscription.level!! < fromSubscription.level!!) {
//            throw SubscriptionUpgradeFailedException("Subscription update level can't be lower than current")
//        }
//        val userSubscriptionEntity = accountSubscriptionRepository.findByAccount_UserId(userId)
//            ?: throw SubscriptionNotFoundException("Not found subscription for this user")
//        val currentUserSubscription = userSubscriptionEntity.subscription!!
//
//
//        val daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), userSubscriptionEntity.validUntil)
//        // TODO: replace 30 days. we need calculate if user take subscription on multiple month
//        val alreadyPayed = currentUserSubscription.price!! - (currentUserSubscription.price!! / 30) * (30 - daysLeft)
//        val newSubPrice = (toSubscription.price!! / 30) * daysLeft
//        val upgradePrice = newSubPrice - alreadyPayed
//
//
//        val accBalance = userSubscriptionEntity.account?.wallet?.balance ?: 0
//        if (accBalance < subscriptionEntity.price!!) {
//            throw AccountBalanceLimitException("Not enough account balance. balance=${accBalance}; price=${subscriptionEntity?.price}")
//        }
//
//        applicationEventPublisher.publishEvent(
//            UpgradeUserSubscriptionEvent(userId, fromSubId.toString(), toSubId.toString())
//        )
//    }

}
