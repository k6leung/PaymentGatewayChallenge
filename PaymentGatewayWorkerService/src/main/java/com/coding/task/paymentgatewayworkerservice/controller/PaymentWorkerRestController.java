package com.coding.task.paymentgatewayworkerservice.controller;

import com.coding.task.common.model.request.TaskExecutionWorkerRequest;
import com.coding.task.paymentgatewayworkerservice.service.PaymentExecutionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class PaymentWorkerRestController {

    private PaymentExecutionService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/payment")
    public void acknowledgePaymentAndWork(@RequestBody TaskExecutionWorkerRequest request) {
        this.service.executePayment(request);
    }
}
