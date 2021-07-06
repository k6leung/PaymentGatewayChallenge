package com.coding.task.paymentgatewaytaskdispatcher.service;

import org.bson.types.ObjectId;

public interface NotificationService {

    void dispatchNotification(ObjectId objectId);

}
