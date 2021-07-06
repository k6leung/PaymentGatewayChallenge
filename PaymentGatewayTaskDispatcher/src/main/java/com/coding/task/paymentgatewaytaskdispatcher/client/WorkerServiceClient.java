package com.coding.task.paymentgatewaytaskdispatcher.client;

import com.coding.task.common.model.request.TaskExecutionWorkerRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "payment-client",
        url = "${worker.host}",
        primary = false
)
public interface WorkerServiceClient {

    @PostMapping(value = "/api/payment", consumes = "application/json")
    void executePayment(TaskExecutionWorkerRequest request);

    @PostMapping(value = "/api/notification", consumes = "application/json")
    void executeNotification(TaskExecutionWorkerRequest request);
}
