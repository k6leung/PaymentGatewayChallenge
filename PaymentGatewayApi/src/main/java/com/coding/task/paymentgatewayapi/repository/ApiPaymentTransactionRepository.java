package com.coding.task.paymentgatewayapi.repository;

import com.coding.task.common.entity.PaymentTransaction;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiPaymentTransactionRepository extends MongoRepository<PaymentTransaction, ObjectId> {
}
