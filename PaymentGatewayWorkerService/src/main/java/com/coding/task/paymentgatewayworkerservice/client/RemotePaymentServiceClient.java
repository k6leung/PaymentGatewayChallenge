package com.coding.task.paymentgatewayworkerservice.client;

import com.coding.task.paymentgatewayworkerservice.model.PaymentRequest;

//this client sends actual payment request to remote payment service
//assume remote payment service is also fully asynchronous
//assume using RestTemplate
public interface RemotePaymentServiceClient {

    void makePayment(PaymentRequest request);

}
