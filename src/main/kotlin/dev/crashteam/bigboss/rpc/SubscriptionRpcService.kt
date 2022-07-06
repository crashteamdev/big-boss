package dev.crashteam.bigboss.rpc

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
import dev.crashteam.bigboss.service.model.AddUserSubscriptionDto
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
                    this.subscriptionId = subscriptionEntity.id.toString()
                }.build()
            }.build()
        } catch (e: IllegalArgumentException) {
            val errorResponse = ErrorCreateSubscriptionResponse.newBuilder()
                .setCode(ErrorCreateSubscriptionResponse.ErrorCode.BAD_REQUEST_PARAMS)
                .setDescription("Failed to create subscription case of bad request")
                .build()
            CreateSubscriptionResponse.newBuilder().setErrorResponse(errorResponse).build()
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
            log.error(e) { "Failed to modify subscription ${request.subscriptionId}" }
            ModifySubscriptionResponse.newBuilder()
                .setErrorResponse(
                    ErrorModifySubscriptionResponse.newBuilder()
                        .setCode(ErrorModifySubscriptionResponse.ErrorCode.UNEXPECTED_ERROR)
                        .setDescription("Failed to modify subscription ${request.subscriptionId}")
                        .build()
                ).build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun addAccountSubscription(
        request: AddAccountSubscriptionRequest,
        responseObserver: StreamObserver<AddAccountSubscriptionResponse>
    ) {
        log.info { "Set account subscription request: $request" }
        val addUserSubscriptionDto = AddUserSubscriptionDto(
            userId = request.userId,
            productId = UUID.fromString(request.productId),
            subscriptionId = UUID.fromString(request.subscriptionId),
            validUntil = LocalDateTime.ofEpochSecond(
                request.validUntil.seconds,
                request.validUntil.nanos,
                ZoneOffset.UTC
            )
        )
        val response = try {
            val accountSubscription: AccountSubscriptionEntity =
                userSubscriptionService.addUserSubscription(addUserSubscriptionDto)
            val successResponse = SuccessAddAccountResponse.newBuilder()
                .setSubscriptionId(accountSubscription.subscription?.id!!.toString())
                .build()
            AddAccountSubscriptionResponse.newBuilder()
                .setSuccessResponse(successResponse).build()
        } catch (e: SubscriptionAlreadyExistsException) {
            val errorResponse = ErrorAddAccountResponse.newBuilder()
                .setCode(ErrorAddAccountResponse.ErrorCode.ALREADY_EXISTS)
                .setDescription(e.message)
                .build()
            AddAccountSubscriptionResponse.newBuilder()
                .setErrorResponse(errorResponse).build()
        } catch (e: AccountBalanceLimitException) {
            val errorResponse = ErrorAddAccountResponse.newBuilder()
                .setCode(ErrorAddAccountResponse.ErrorCode.NOT_ENOUGH_CREDIT)
                .setDescription(e.message)
                .build()
            AddAccountSubscriptionResponse.newBuilder()
                .setErrorResponse(errorResponse).build()
        } catch (e: Exception) {
            log.error(e) { "Can't create or update user subscription" }
            val errorResponse = ErrorAddAccountResponse.newBuilder()
                .setCode(ErrorAddAccountResponse.ErrorCode.UNEXPECTED_ERROR)
                .setDescription("Unknown error. Can't create or update user subscription.")
                .build()
            AddAccountSubscriptionResponse.newBuilder()
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
                .setSubscriptionId(removeUserSubscriptionDto.subscriptionId.toString())
                .setUserId(removeUserSubscriptionDto.userId)
                .build()
            RemoveAccountSubscriptionResponse.newBuilder()
                .setSuccessResponse(successResponse)
                .build()
        } catch (e: Exception) {
            log.error(e) { "Failed to remove account subscription. subscriptionId=${request.subscriptionId}; userId=${request.userId}" }
            val errorResponse = ErrorRemoveAccountSubscriptionResponse.newBuilder()
                .setCode(ErrorRemoveAccountSubscriptionResponse.ErrorCode.UNEXPECTED_ERROR)
                .setDescription("Failed to remove account subscription. subId=${request.subscriptionId}; userId=${request.userId}")
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
        request: GetAccountSubscriptionRequest,
        responseObserver: StreamObserver<GetAccountSubscriptionResponse>
    ) {
        log.info { "Get account subscription: $request" }
        val accountSubscriptions = userSubscriptionService.getAccountSubscriptions(request.userId)
        val response = if (accountSubscriptions.isNullOrEmpty()) {
            GetAccountSubscriptionResponse.newBuilder()
                .setErrorResponse(ErrorGetAccountSubscriptionResponse.newBuilder().apply {
                    this.code = ErrorGetAccountSubscriptionResponse.ErrorCode.NOT_FOUND
                    this.description = "Not found user by userId: ${request.userId}"
                })
                .build()
        } else {
            val accountProducts = accountSubscriptions.map { accountSubscriptionEntity ->
                AccountProduct.newBuilder().apply {
                    this.product = AccountProduct.ProductInfo.newBuilder().apply {
                        this.productId = accountSubscriptionEntity.subscription?.product?.id.toString()
                        this.name = accountSubscriptionEntity.subscription?.product?.name
                    }.build()
                    this.subscription = AccountProduct.SubscriptionInfo.newBuilder().apply {
                        this.subscriptionId = accountSubscriptionEntity.subscription?.id.toString()
                        this.name = accountSubscriptionEntity.subscription?.name
                    }.build()
                    val validUntil = accountSubscriptionEntity.validUntil!!.toInstant(ZoneOffset.UTC)
                    this.validUntil =
                        Timestamp.newBuilder().setSeconds(validUntil.epochSecond).setNanos(validUntil.nano).build()
                }.build()
            }
            GetAccountSubscriptionResponse.newBuilder()
                .setSuccessResponse(SuccessGetAccountSubscriptionResponse.newBuilder().apply {
                    this.userId = request.userId
                    this.addAllAccountProducts(accountProducts)
                }).build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun getAllSubscription(
        request: GetAllSubscriptionRequest,
        responseObserver: StreamObserver<GetAllSubscriptionResponse>
    ) {
        log.info { "Get all subscriptions: $request" }
        val subscriptionList = subscriptionRepository.findByProduct_Id(UUID.fromString(request.productId))
        val allSubscriptionResponse = GetAllSubscriptionResponse.newBuilder().apply {
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
