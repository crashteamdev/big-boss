package dev.crashteam.bigboss.rpc

import com.google.protobuf.Empty
import com.google.protobuf.Timestamp
import dev.crashteam.bigboss.repository.SubscriptionRepository
import dev.crashteam.bigboss.repository.entity.AccountSubscriptionEntity
import dev.crashteam.bigboss.service.SubscriptionService
import dev.crashteam.bigboss.service.UserSubscriptionService
import dev.crashteam.bigboss.service.error.AccountBalanceLimitException
import dev.crashteam.bigboss.service.error.SubscriptionAlreadyExistsException
import dev.crashteam.bigboss.service.model.CreateSubscriptionDto
import dev.crashteam.bigboss.service.model.ModifySubscriptionDto
import dev.crashteam.bigboss.service.model.RemoveUserSubscriptionDto
import dev.crashteam.bigboss.service.model.SetUserSubscriptionDto
import dev.crashteam.subscription.*
import io.grpc.stub.StreamObserver
import mu.KotlinLogging
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.core.convert.ConversionService
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@GrpcService
class SubscriptionRpcService(
    private val conversationService: ConversionService,
    private val subscriptionService: SubscriptionService,
    private val userSubscriptionService: UserSubscriptionService,
    private val subscriptionRepository: SubscriptionRepository
) : SubscriptionServiceGrpc.SubscriptionServiceImplBase() {

    override fun createSubscription(
        request: CreateSubscriptionRequest,
        responseObserver: StreamObserver<CreateSubscriptionResponse>
    ) {
        log.info { "Create subscription request: $request" }
        val createSubscriptionDto = conversationService.convert(request, CreateSubscriptionDto::class.java)!!
        val response = try {
            val subscriptionEntity = subscriptionService.createSubscription(createSubscriptionDto)
            CreateSubscriptionResponse.newBuilder().apply {
                this.successResponse = SuccessCreateSubscriptionResponse.newBuilder().apply {
                    this.subId = subscriptionEntity.id.toString()
                }.build()
            }.build()
        } catch (e: Exception) {
            log.error(e) { "Can't create subscription" }
            val errorResponse = ErrorCreateSubscriptionResponse.newBuilder()
                .setCode(ErrorCreateSubscriptionResponse.ErrorCode.UNEXPECTED_ERROR)
                .setDescription("Unknown error. Can't create subscription.")
                .build()
            CreateSubscriptionResponse.newBuilder().setErrorResponse(errorResponse).build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun modifySubscription(
        request: ModifySubscriptionRequest,
        responseObserver: StreamObserver<ModifySubscriptionResponse>
    ) {
        log.info { "Modify subscription request: $request" }
        val modifySubscriptionDto = conversationService.convert(request, ModifySubscriptionDto::class.java)!!
        val response = try {
            val modifySubscription = subscriptionService.modifySubscription(modifySubscriptionDto)
            if (modifySubscription == null) {
                val errorModifySubscriptionResponse = ErrorModifySubscriptionResponse.newBuilder()
                    .setCode(ErrorModifySubscriptionResponse.ErrorCode.BAD_REQUEST_PARAMS)
                    .setDescription("Subscription not found").build()
                ModifySubscriptionResponse.newBuilder().setErrorResponse(errorModifySubscriptionResponse).build()
            } else {
                val resultSubscription = conversationService.convert(modifySubscription, Subscription::class.java)
                val subscriptionResponse = SuccessModifySubscriptionResponse.newBuilder()
                    .setSubscription(resultSubscription).build()
                ModifySubscriptionResponse.newBuilder().setSuccessResponse(subscriptionResponse).build()
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to modify subscription ${request.subId}" }
            ModifySubscriptionResponse.newBuilder()
                .setErrorResponse(
                    ErrorModifySubscriptionResponse.newBuilder()
                        .setCode(ErrorModifySubscriptionResponse.ErrorCode.UNEXPECTED_ERROR)
                        .setDescription("Failed to modify subscription ${request.subId}")
                        .build()
                ).build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun setAccountSubscription(
        request: SetAccountSubscriptionRequest,
        responseObserver: StreamObserver<SetAccountSubscriptionResponse>
    ) {
        log.info { "Set account subscription request: $request" }
        val setUserSubscriptionDto = SetUserSubscriptionDto(
            userId = request.userId,
            subId = UUID.fromString(request.subId),
            validUntil = LocalDateTime.ofEpochSecond(
                request.validUntil.seconds,
                request.validUntil.nanos,
                ZoneOffset.UTC
            )
        )
        val response = try {
            val accountSubscription: AccountSubscriptionEntity =
                userSubscriptionService.setUserSubscription(setUserSubscriptionDto)
            val successResponse = SuccessSetAccountResponse.newBuilder()
                .setSubscriptionId(accountSubscription.subscription?.id!!.toString())
                .build()
            SetAccountSubscriptionResponse.newBuilder()
                .setSuccessResponse(successResponse).build()
        } catch (e: SubscriptionAlreadyExistsException) {
            val errorResponse = ErrorSetAccountResponse.newBuilder()
                .setCode(ErrorSetAccountResponse.ErrorCode.ALREADY_EXISTS)
                .setDescription(e.message)
                .build()
            SetAccountSubscriptionResponse.newBuilder()
                .setErrorResponse(errorResponse).build()
        } catch (e: AccountBalanceLimitException) {
            val errorResponse = ErrorSetAccountResponse.newBuilder()
                .setCode(ErrorSetAccountResponse.ErrorCode.NOT_ENOUGH_CREDIT)
                .setDescription(e.message)
                .build()
            SetAccountSubscriptionResponse.newBuilder()
                .setErrorResponse(errorResponse).build()
        } catch (e: Exception) {
            log.error(e) { "Can't create or update user subscription" }
            val errorResponse = ErrorSetAccountResponse.newBuilder()
                .setCode(ErrorSetAccountResponse.ErrorCode.UNEXPECTED_ERROR)
                .setDescription("Unknown error. Can't create or update user subscription.")
                .build()
            SetAccountSubscriptionResponse.newBuilder()
                .setErrorResponse(errorResponse).build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun removeAccountSubscription(
        request: RemoveAccountSubscriptionRequest,
        responseObserver: StreamObserver<RemoveAccountSubscriptionResponse>
    ) {
        log.info { "Remove account subscription: $request" }
        val response = try {
            val removeUserSubscriptionDto = conversationService.convert(request, RemoveUserSubscriptionDto::class.java)
            userSubscriptionService.removeUserSubscription(removeUserSubscriptionDto!!)
            val successResponse = SuccessRemoveAccountSubscriptionResponse.newBuilder()
                .setSubId(removeUserSubscriptionDto.subId.toString())
                .setUserId(removeUserSubscriptionDto.userId)
                .build()
            RemoveAccountSubscriptionResponse.newBuilder()
                .setSuccessResponse(successResponse)
                .build()
        } catch (e: Exception) {
            log.error(e) { "Failed to remove account subscription. subId=${request.subId}; userId=${request.subId}" }
            val errorResponse = ErrorRemoveAccountSubscriptionResponse.newBuilder()
                .setCode(ErrorRemoveAccountSubscriptionResponse.ErrorCode.UNEXPECTED_ERROR)
                .setDescription("Failed to remove account subscription. subId=${request.subId}; userId=${request.subId}")
                .build()
            RemoveAccountSubscriptionResponse.newBuilder()
                .setErrorResponse(errorResponse)
                .build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun upgradeAccountSubscription(
        request: UpgradeAccountSubscriptionRequest,
        responseObserver: StreamObserver<UpgradeAccountSubscriptionRequest>
    ) {
        TODO("Upgrade not yet implemented")
    }

    override fun getAccountSubscription(
        request: GetSubscriptionRequest,
        responseObserver: StreamObserver<GetSubscriptionResponse>
    ) {
        log.info { "Get account subscription: $request" }
        val accountSubscription = userSubscriptionService.getAccountSubscription(request.subId)
        val response = if (accountSubscription == null) {
            GetSubscriptionResponse.newBuilder()
                .setErrorResponse(ErrorGetSubscriptionResponse.newBuilder().apply {
                    this.code = ErrorGetSubscriptionResponse.ErrorCode.NOT_FOUND
                    this.description = "Not found user subscription by id: ${request.subId}"
                })
                .build()
        } else {
            GetSubscriptionResponse.newBuilder()
                .setSuccessResponse(SuccessGetSubscriptionResponse.newBuilder().apply {
                    this.plan = AccountSubscription.newBuilder().apply {
                        val subscription =
                            conversationService.convert(accountSubscription.subscription, Subscription::class.java)
                        this.userId = accountSubscription.account!!.userId
                        this.subscription = subscription
                        val validUntil = accountSubscription.validUntil!!.toInstant(ZoneOffset.UTC)
                        this.validUntil =
                            Timestamp.newBuilder().setSeconds(validUntil.epochSecond).setNanos(validUntil.nano).build()
                    }.build()
                }).build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun getAllSubscription(request: Empty, responseObserver: StreamObserver<AllSubscriptionResponse>) {
        log.info { "Get all subscriptions" }
        val subscriptionList = subscriptionRepository.findAll()
        val allSubscriptionResponse = AllSubscriptionResponse.newBuilder().apply {
            for (subscriptionEntity in subscriptionList) {
                val subscription = conversationService.convert(subscriptionEntity, Subscription::class.java)
                addPlan(subscription)
            }
        }.build()
        responseObserver.onNext(allSubscriptionResponse)
        responseObserver.onCompleted()
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
