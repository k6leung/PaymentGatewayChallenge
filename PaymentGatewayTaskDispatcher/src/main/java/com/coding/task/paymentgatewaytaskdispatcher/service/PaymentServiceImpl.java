package com.coding.task.paymentgatewaytaskdispatcher.service;

import com.coding.task.common.model.request.TaskExecutionWorkerRequest;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.paymentgatewaytaskdispatcher.client.WorkerServiceClient;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private WorkerServiceClient workerServiceClient;

    private UpdateExecutionStateService updateExecutionStateService;

    @Override
    @Async(value = "paymentTaskExecutor")
    public void dispatchPayment(ObjectId objectId) {
        String objectIdStr = objectId.toString();
        log.info("Dispatching payment with id: {}", objectIdStr);
        try {
            this.workerServiceClient.executePayment(
                    new TaskExecutionWorkerRequest(objectIdStr)
            );

            log.info("Payment dispatch for id {} complete.", objectIdStr);
        } catch (FeignException fe) {
            log.error(String.format("Payment dispatch with id %s failed.", objectIdStr)
                    , fe);
            this.updateExecutionStateService.updateExecutionState(objectId,
                    ExecutionState.PAYMENT_REQUEST_TECHNICAL_FAILED);
        }
    }
}
