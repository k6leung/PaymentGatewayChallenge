package com.coding.task.paymentgatewayworkerservice.controller;

import com.coding.task.common.model.request.TaskExecutionWorkerRequest;
import com.coding.task.paymentgatewayworkerservice.service.NotificationExecutionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class NotificationWorkerRestController {

    private NotificationExecutionService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/notification")
    public void acknowledgeNotificationAndWork(@RequestBody TaskExecutionWorkerRequest request) {
        this.service.executeNotification(request);
    }
}
