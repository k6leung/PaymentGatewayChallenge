package com.coding.task.paymentgatewayworkerservice.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.common.model.request.TaskExecutionWorkerRequest;
import com.coding.task.paymentgatewayworkerservice.client.RemotePaymentServiceClient;
import com.coding.task.paymentgatewayworkerservice.model.PaymentRequest;
import com.coding.task.paymentgatewayworkerservice.repository.WorkerPaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionServiceImpl implements PaymentExecutionService {

    @Value("${payment.callback.uri}")
    private String paymentCallbackUri;

    @Value("${payment.callback.method}")
    private String paymentCallbackMethod;

    private final ExecutionStateUpdatingService executionStateUpdatingService;

    private final WorkerPaymentTransactionRepository repository;

    private final RemotePaymentServiceClient client;

    @Async
    @Override
    public void executePayment(TaskExecutionWorkerRequest request) {
        String transactionIdStr = request.getTransactionId();
        log.info("Execute payment for transaction id: {}", transactionIdStr);

        ObjectId transactionId = new ObjectId(transactionIdStr);
        Optional<PaymentTransaction> transactionOptional = this.repository.findById(transactionId);

        if(transactionOptional.isPresent()) {
            PaymentTransaction transaction = transactionOptional.get();
            try {
                LocalDateTime now = LocalDateTime.now();

                transaction.setExecutionState(ExecutionState.REQUESTING_FOR_PAYMENT);
                int attempt = transaction.getPaymentAttempts();
                transaction.setPaymentAttempts(++attempt);
                transaction.setPaymentExecutionStartTime(now);
                this.repository.save(transaction);

                this.client.makePayment(new PaymentRequest(transactionIdStr,
                        this.paymentCallbackUri,
                        this.paymentCallbackMethod));
            } catch (ResourceAccessException rae) {
                //if timeout, we set a timeout state so that remote payment gateway may still be able
                //to update payment status
                ExecutionState newState = (rae.getCause() instanceof SocketTimeoutException) ?
                        ExecutionState.PAYMENT_REQUEST_TIMEOUT :
                        ExecutionState.PAYMENT_REQUEST_TECHNICAL_FAILED;

                this.executionStateUpdatingService.updateExecutionState(transactionId, newState);
            } catch (RestClientResponseException rcre) {
                int httpStatusCode = rcre.getRawStatusCode();
                log.error(
                        String.format("Http status %s detected during payment for transaction id: %s",
                                httpStatusCode,
                                transactionIdStr),
                        rcre);

                ExecutionState newState = ExecutionState.PAYMENT_REQUEST_TECHNICAL_FAILED;
                //assume 409 is business error, other 4xx are request content level/security errors
                if (HttpStatus.valueOf(httpStatusCode).is4xxClientError()) {
                    log.error("Payment execution 4xx error detected during payment for transaction id: {}", transactionIdStr);
                    newState = ExecutionState.PAYMENT_REQUEST_UNRECOVERABLE_FAILED;
                }

                this.executionStateUpdatingService.updateExecutionState(transactionId, newState);
            } catch (RuntimeException re) {
                this.executionStateUpdatingService.updateExecutionState(transactionId,
                        ExecutionState.PAYMENT_REQUEST_TECHNICAL_FAILED);
            }
        }

    }
}
