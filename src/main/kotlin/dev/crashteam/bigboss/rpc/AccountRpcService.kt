package dev.crashteam.bigboss.rpc

import com.google.protobuf.Timestamp
import dev.crashteam.account.*
import dev.crashteam.bigboss.service.AccountService
import dev.crashteam.subscription.AccountProduct
import io.grpc.stub.StreamObserver
import mu.KotlinLogging
import net.devh.boot.grpc.server.service.GrpcService
import java.time.ZoneOffset

@GrpcService
class AccountRpcService(
    private val accountService: AccountService
) : AccountServiceGrpc.AccountServiceImplBase() {

    override fun getAccountInfo(request: AccountInfoRequest, responseObserver: StreamObserver<AccountInfoResponse>) {
        log.info { "Get account info: $request" }
        val accountEntity = accountService.getAccount(request.userId)
        val response = try {
            if (accountEntity != null) {
                val accountProducts = accountEntity.accountSubscriptions?.map { accountSubscriptionEntity ->
                    AccountProduct.newBuilder().apply {
                        this.product = AccountProduct.ProductInfo.newBuilder().apply {
                            this.productId = accountSubscriptionEntity.subscription!!.product!!.id.toString()
                            this.name = accountSubscriptionEntity.subscription!!.product!!.name
                        }.build()
                        this.subscription = AccountProduct.SubscriptionInfo.newBuilder().apply {
                            this.subscriptionId = accountSubscriptionEntity.subscription!!.id.toString()
                            this.name = accountSubscriptionEntity.subscription!!.name
                        }.build()
                        val validUntilInstant = accountSubscriptionEntity.validUntil!!.toInstant(ZoneOffset.UTC)
                        this.validUntil = Timestamp.newBuilder().setSeconds(validUntilInstant.epochSecond)
                            .setNanos(validUntilInstant.nano).build()
                    }.build()
                }
                AccountInfoResponse.newBuilder().apply {
                    this.successResponse = SuccessAccountInfoResponse.newBuilder().apply {
                        this.account = Account.newBuilder().apply {
                            this.userId = accountEntity.userId
                            this.email = accountEntity.email
                            this.blocked = accountEntity.blocked
                            if (accountProducts != null && accountProducts.isNotEmpty()) {
                                this.addAllProducts(accountProducts)
                            }
                        }.build()
                    }.build()
                }.build()
            } else {
                AccountInfoResponse.newBuilder().apply {
                    this.errorResponse = ErrorAccountInfoResponse.newBuilder().apply {
                        this.code = ErrorAccountInfoResponse.ErrorCode.USER_NOT_FOUND
                        this.description = "Can't find user by id: ${request.userId}"
                    }.build()
                }.build()
            }
        } catch (e: Exception) {
            AccountInfoResponse.newBuilder().apply {
                this.errorResponse = ErrorAccountInfoResponse.newBuilder().apply {
                    this.code = ErrorAccountInfoResponse.ErrorCode.UNEXPECTED_ERROR
                    this.description = "Unexpected exception during getting account info"
                }.build()
            }.build()
        }
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
