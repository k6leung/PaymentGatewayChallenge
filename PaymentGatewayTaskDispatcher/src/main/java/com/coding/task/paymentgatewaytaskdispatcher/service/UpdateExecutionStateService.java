package com.coding.task.paymentgatewaytaskdispatcher.service;

import com.coding.task.common.enums.ExecutionState;
import org.bson.types.ObjectId;

public interface UpdateExecutionStateService {

    void updateExecutionState(ObjectId id, ExecutionState newState);

}
