package com.coding.task.paymentgatewayworkerservice.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.paymentgatewayworkerservice.repository.WorkerPaymentTransactionRepository;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ExecutionStateUpdatingServiceImpl implements ExecutionStateUpdatingService {

    private WorkerPaymentTransactionRepository repository;

    @Override
    @Transactional
    public void updateExecutionState(ObjectId id, ExecutionState newState) {
        Optional<PaymentTransaction> transactionOptional = this.repository.findById(id);

        if(transactionOptional.isPresent()) {
            PaymentTransaction transaction = transactionOptional.get();
            transaction.setExecutionState(newState);
            this.repository.save(transaction);
        }
    }
}
