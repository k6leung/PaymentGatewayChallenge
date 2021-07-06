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
public class NotificationServiceImpl implements NotificationService {

    private WorkerServiceClient workerServiceClient;

    private UpdateExecutionStateService updateExecutionStateService;

    @Override
    @Async(value = "notificationTaskExecutor")
    public void dispatchNotification(ObjectId objectId) {
        String objectIdStr = objectId.toString();
        log.info("Dispatching notification with id: {}", objectIdStr);

        try {
            this.workerServiceClient.executeNotification(
                    new TaskExecutionWorkerRequest(objectIdStr)
            );

            log.info("Notification dispatch for id {} complete.", objectIdStr);
        } catch (FeignException fe) {
            log.error(String.format("Notification dispatch with id %s failed.", objectIdStr)
                    , fe);
            this.updateExecutionStateService.updateExecutionState(objectId,
                    ExecutionState.RESPONSE_SENDING_TECHNICAL_FAILED);
        }
    }
}
