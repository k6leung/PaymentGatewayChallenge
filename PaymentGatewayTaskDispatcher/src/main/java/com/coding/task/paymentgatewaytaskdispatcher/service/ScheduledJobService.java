package com.coding.task.paymentgatewaytaskdispatcher.service;

public interface ScheduledJobService {

    void runPaymentJob();

    void runNotificationJob();

    void expireClaim();

}
