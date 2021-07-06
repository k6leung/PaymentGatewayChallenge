package com.coding.task.paymentgatewayapi.controller;

import com.coding.task.paymentgatewayapi.model.request.IncomingPaymentRequest;
import com.coding.task.paymentgatewayapi.model.request.PaymentResultUpdateRequest;
import com.coding.task.paymentgatewayapi.model.response.IncomingPaymentAcknowledgementResponse;
import com.coding.task.paymentgatewayapi.service.PaymentTransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

//for simplicity
//omit logging
//no servlet filter/handler interceptor for request/response logging
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class PaymentTransactionRestController {

    private PaymentTransactionService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/payment",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public IncomingPaymentAcknowledgementResponse createPaymentRecord(@Valid
                                                                      @RequestBody
                                                                      IncomingPaymentRequest request) {
        return this.service.storePaymentRequest(request);
    }

    //assume request body format is agreed by the external payment gateway during integration phase
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(value = "/payment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void updatePaymentRecord(@Valid
                                    @RequestBody
                                    PaymentResultUpdateRequest request) {
        this.service.updatePaymentTransaction(request);
    }
}
