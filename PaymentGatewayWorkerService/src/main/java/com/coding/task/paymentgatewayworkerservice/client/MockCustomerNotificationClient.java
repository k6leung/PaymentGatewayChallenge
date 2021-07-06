package com.coding.task.paymentgatewayworkerservice.client;

import com.coding.task.common.entity.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

//mock implementation
//this client notifies our client's server (customer) about payment result
//assume client server is asynchronous
//assume using RestTemplate
@Profile("!test")
@Slf4j
@Service
@AllArgsConstructor
public class MockCustomerNotificationClient implements CustomerNotificationClient {

    @Override
    public void notifyCustomer(PaymentTransaction transaction) {
        log.info("Customer notified for transaction id: {}", transaction.getId().toString());
    }
}
