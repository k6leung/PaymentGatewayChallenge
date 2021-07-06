package com.coding.task.paymentgatewayworkerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//this worker application can be run in massively parallel fashion (scale out)
//assuming this app will be deployed on pcf/kubernetes, load balanced by pcf route/kubernetes service
//as internal worker, it will only acknowledge work, asynchronously perform work and self handle errors
//hence no controller advice needed
@SpringBootApplication
public class PaymentGatewayWorkerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentGatewayWorkerServiceApplication.class, args);
    }

}
