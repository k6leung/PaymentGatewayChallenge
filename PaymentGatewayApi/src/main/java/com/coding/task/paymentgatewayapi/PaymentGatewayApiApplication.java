package com.coding.task.paymentgatewayapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//this application can be run in massively parallel fashion (scale out)
//assuming this app will be deployed on pcf/kubernetes, load balanced by pcf route/kubernetes service
@SpringBootApplication
public class PaymentGatewayApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentGatewayApiApplication.class, args);
    }

}
