package com.coding.task.paymentgatewaytaskdispatcher.repository;

import com.coding.task.common.entity.PaymentTransaction;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DispatcherPaymentTransactionRepositoryDispatchClaim extends MongoRepository<PaymentTransaction, ObjectId>,
        DispatchClaimBulkUpdateRepository {

    @Query(value = "{\n" +
            "    $or:[\n" +
            "        {'executionState':'PENDING_FOR_EXECUTION'},\n" +
            "        {\n" +
            "            'executionState':'PAYMENT_REQUEST_TIMEOUT',\n" +
            "            'paymentAttempts': {$lt:?0},\n" +
            "            'paymentExecutionStartTime': {$lte:?1}\n" +
            "        },\n" +
            "        {\n" +
            "            'executionState':'PAYMENT_REQUEST_TECHNICAL_FAILED',\n" +
            "            'paymentAttempts': {$lt:?0},\n" +
            "            'paymentExecutionStartTime': {$lte:?2}\n" +
            "        }\n" +
            "    ]\n" +
            "}",
            fields = "{'_id':1}",
            sort = "{'transactionDetails.receiveTime':1}"
    )
    List<PaymentTransaction> findTransactionForPayment(int maxPaymentAttemptCount,
                                                       LocalDateTime paymentTimeoutBackoffBefore,
                                                       LocalDateTime paymentTechnicalErrorBackoffBefore,
                                                       PageRequest pageRequest);

    @Query(value = "{\n" +
            "    $or:[\n" +
            "        {\"executionState\":\"RESULT_RECEIVED\"},\n" +
            "        {\n" +
            "            \"executionState\":\"RESPONSE_SENDING_TIMEOUT\",\n" +
            "            \"notificationSendingAttempts\": {$lt:?0},\n" +
            "            \"notificationExecutionStartTime\": {$lte:?1}\n" +
            "        },\n" +
            "        {\n" +
            "            \"executionState\":\"RESPONSE_SENDING_TECHNICAL_FAILED\",\n" +
            "            \"notificationSendingAttempts\": {$lt:?0},\n" +
            "            \"notificationExecutionStartTime\": {$lte:?2}\n" +
            "        }\n" +
            "    ]\n" +
            "}",
            fields = "{'_id':1}",
            sort = "{'transactionDetails.receiveTime':1}"
    )
    List<PaymentTransaction> findTransactionForNotification(int maxNotificationAttemptCount,
                                                            LocalDateTime notificationTimeoutBackoffBefore,
                                                            LocalDateTime notificationTechnicalErrorBackoffBefore,
                                                            PageRequest pageRequest);

    @Query(value = "{\n" +
            "    'executionState':'PAYMENT_EXECUTION_CLAIMED',\n" +
            "    'paymentDispatchClaimTime':{$lte:?0}\n" +
            "}",
            fields = "{'_id':1}"
    )
    List<PaymentTransaction> findTransactionForPaymentUnclaim(LocalDateTime paymentClaimBefore,
                                                              PageRequest pageRequest);

    @Query(value = "{\n" +
            "    'executionState':'RESPONSE_SENDING_EXECUTION_CLAIMED',\n" +
            "    'notificationDispatchClaimTime':{$lte:?0}\n" +
            "}",
            fields = "{'_id':1}"
    )
    List<PaymentTransaction> findTransactionForNotificationUnclaim(LocalDateTime notificationClaimBefore,
                                                                   PageRequest pageRequest);
}
