package com.coding.task.paymentgatewaytaskdispatcher.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.paymentgatewaytaskdispatcher.repository.DispatcherPaymentTransactionRepositoryDispatchClaim;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UpdateExecutionStateServiceImpl implements UpdateExecutionStateService {

    private DispatcherPaymentTransactionRepositoryDispatchClaim repository;

    @Override
    public void updateExecutionState(ObjectId id, ExecutionState newState) {
        Optional<PaymentTransaction> transactionOptional = this.repository.findById(id);

        //not remote service fail, should not increment attempt count
        if(transactionOptional.isPresent()) {
            PaymentTransaction transaction = transactionOptional.get();
            transaction.setExecutionState(newState);
            this.repository.save(transaction);
        }
    }
}
