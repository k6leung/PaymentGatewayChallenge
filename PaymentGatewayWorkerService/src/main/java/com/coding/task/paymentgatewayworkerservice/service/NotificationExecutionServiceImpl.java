package com.coding.task.paymentgatewayworkerservice.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.common.model.request.TaskExecutionWorkerRequest;
import com.coding.task.paymentgatewayworkerservice.client.CustomerNotificationClient;
import com.coding.task.paymentgatewayworkerservice.repository.WorkerPaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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
public class NotificationExecutionServiceImpl implements NotificationExecutionService {

    private final ExecutionStateUpdatingService executionStateUpdatingService;

    private final WorkerPaymentTransactionRepository repository;

    private final CustomerNotificationClient client;

    @Async
    @Override
    public void executeNotification(TaskExecutionWorkerRequest request) {
        String transactionIdStr = request.getTransactionId();
        log.info("Execute notification for transaction id: {}", transactionIdStr);

        ObjectId transactionId = new ObjectId(transactionIdStr);
        Optional<PaymentTransaction> transactionOptional = this.repository.findById(transactionId);

        if(transactionOptional.isPresent()) {
            PaymentTransaction transaction = transactionOptional.get();
            try {
                LocalDateTime now = LocalDateTime.now();

                transaction.setExecutionState(ExecutionState.RESPONSE_SENT);
                int attempt = transaction.getNotificationSendingAttempts();
                transaction.setNotificationSendingAttempts(++attempt);
                transaction.setNotificationExecutionStartTime(now);
                this.repository.save(transaction);

                this.client.notifyCustomer(transaction);
            }  catch (ResourceAccessException rae) {
                //if timeout, we set a timeout state so that remote payment gateway may still be able
                //to update payment status
                ExecutionState newState = (rae.getCause() instanceof SocketTimeoutException) ?
                        ExecutionState.RESPONSE_SENDING_TIMEOUT :
                        ExecutionState.RESPONSE_SENDING_TECHNICAL_FAILED;

                this.executionStateUpdatingService.updateExecutionState(transactionId, newState);
            } catch (RestClientResponseException rcre) {
                int httpStatusCode = rcre.getRawStatusCode();
                log.error(
                        String.format("Http status %s detected during notification for transaction id: %s",
                                httpStatusCode,
                                transactionIdStr),
                        rcre);

                ExecutionState newState = ExecutionState.RESPONSE_SENDING_TECHNICAL_FAILED;
                //assume 409 is business error, other 4xx are request content level/security errors
                if (HttpStatus.valueOf(httpStatusCode).is4xxClientError()) {
                    log.error("Payment execution 4xx error detected during notification for transaction id: {}", transactionIdStr);
                    newState = ExecutionState.RESPONSE_SENDING_UNRECOVERABLE_FAILED;
                }

                this.executionStateUpdatingService.updateExecutionState(transactionId, newState);
            } catch (RuntimeException re) {
                this.executionStateUpdatingService.updateExecutionState(transactionId,
                        ExecutionState.RESPONSE_SENDING_TECHNICAL_FAILED);
            }

        }
    }
}
