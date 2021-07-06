package com.coding.task.paymentgatewayworkerservice.client;

import com.coding.task.common.entity.PaymentTransaction;

//this client notifies our client's server (customer) about payment result
//assume client server is asynchronous
//assume using RestTemplate
public interface CustomerNotificationClient {

    void notifyCustomer(PaymentTransaction transaction);

}
