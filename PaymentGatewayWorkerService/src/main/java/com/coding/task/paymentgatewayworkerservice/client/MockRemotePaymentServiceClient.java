package com.coding.task.paymentgatewayworkerservice.client;

import com.coding.task.paymentgatewayworkerservice.model.PaymentRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

//mock implementation
//this client sends actual payment request to remote payment service
//assume remote payment service is also fully asynchronous
//assume using RestTemplate
@Profile("!test")
@Slf4j
@Service
@AllArgsConstructor
public class MockRemotePaymentServiceClient implements RemotePaymentServiceClient {

    @Override
    public void makePayment(PaymentRequest request) {
        log.info("Payment made for transaction id: {}", request.getExternalReferenceId());
    }

}
