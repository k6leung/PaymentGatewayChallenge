package com.coding.task.paymentgatewaytaskdispatcher.repository;

import com.coding.task.common.enums.ExecutionState;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public interface DispatchClaimBulkUpdateRepository {

    void claimTransactionForDispatch(ExecutionState newState,
                                     String claimTimeKey,
                                     LocalDateTime claimTime,
                                     ObjectId[] ids);

    void unclaimTransaction(ExecutionState newState,
                            ObjectId[] ids);

}
