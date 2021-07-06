package com.coding.task.paymentgatewayworkerservice.repository;

import com.coding.task.common.entity.PaymentTransaction;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkerPaymentTransactionRepository extends MongoRepository<PaymentTransaction, ObjectId> {
}
