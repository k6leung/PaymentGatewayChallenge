package com.coding.task.paymentgatewaytaskdispatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

//this application can be turned into spring batch...
//but since I don't have sql db setup and for simplicity reasons, we will use basic spring scheduling

//IMPORTANT: THIS TASK DISPATCHER APPLICATION SHOULD BE A SINGLETON
@EnableFeignClients
@SpringBootApplication
public class PaymentGatewayTaskDispatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentGatewayTaskDispatcherApplication.class, args);
    }

}
