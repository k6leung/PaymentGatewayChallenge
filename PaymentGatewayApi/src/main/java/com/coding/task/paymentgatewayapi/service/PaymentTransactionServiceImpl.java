package com.coding.task.paymentgatewayapi.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.paymentgatewayapi.exception.InvalidTransactionStateException;
import com.coding.task.paymentgatewayapi.exception.TransactionNotFoundException;
import com.coding.task.paymentgatewayapi.mapper.PaymentTransactionMapper;
import com.coding.task.paymentgatewayapi.model.request.IncomingPaymentRequest;
import com.coding.task.paymentgatewayapi.model.request.PaymentResultUpdateRequest;
import com.coding.task.paymentgatewayapi.model.response.IncomingPaymentAcknowledgementResponse;
import com.coding.task.paymentgatewayapi.repository.ApiPaymentTransactionRepository;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

//for simplicity
//omit logging
@Service
@AllArgsConstructor
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private ApiPaymentTransactionRepository repository;

    private PaymentTransactionMapper mapper;

    @Override
    @Transactional
    public IncomingPaymentAcknowledgementResponse storePaymentRequest(IncomingPaymentRequest request) {
        LocalDateTime receiveTime = LocalDateTime.now();

        PaymentTransaction transaction = this.mapper.mapTransactionRequestForPost(request);
        transaction.setExecutionState(ExecutionState.PENDING_FOR_EXECUTION);
        transaction.setPaymentAttempts(0);
        transaction.setNotificationSendingAttempts(0);

        transaction = this.repository.save(transaction);

        return new IncomingPaymentAcknowledgementResponse(
                transaction.getId().toString(),
                receiveTime
        );
    }

    @Override
    @Transactional
    public void updatePaymentTransaction(PaymentResultUpdateRequest request) {
        //find transaction from db
        ObjectId id = new ObjectId(request.getId());
        PaymentTransaction transactionFromDb = this.repository.findById(id)
                .orElseThrow(
                        () -> new TransactionNotFoundException("Transaction not found", id.toString())
        );
        //check record status for eligibility to update
        //REQUESTING_FOR_PAYMENT or PAYMENT_REQUEST_TIMEOUT are eligible - PAYMENT_REQUEST_TIMEOUT can be just remote server slow
        //NOTE: cannot check paymentCompleteTime because remote payment service server time
        //      is not synchronized with our server
        ExecutionState executionState = transactionFromDb.getExecutionState();
        if((ExecutionState.REQUESTING_FOR_PAYMENT != executionState) &&
                (ExecutionState.PAYMENT_REQUEST_TIMEOUT != executionState)) {
            throw new InvalidTransactionStateException("Transaction cannot be updated", id.toString());
        }
        //do update
        this.mapper.mapTransactionUpdateForPatch(request, transactionFromDb);
        transactionFromDb.setExecutionState(ExecutionState.RESULT_RECEIVED);

        this.repository.save(transactionFromDb);
    }
}
