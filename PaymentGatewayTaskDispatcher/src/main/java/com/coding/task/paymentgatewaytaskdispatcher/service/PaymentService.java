package com.coding.task.paymentgatewaytaskdispatcher.service;

import org.bson.types.ObjectId;

public interface PaymentService {

    void dispatchPayment(ObjectId objectId);

}
