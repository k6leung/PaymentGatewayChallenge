package com.coding.task.paymentgatewayworkerservice.service;

import com.coding.task.common.model.request.TaskExecutionWorkerRequest;

public interface NotificationExecutionService {

    void executeNotification(TaskExecutionWorkerRequest request);

}
