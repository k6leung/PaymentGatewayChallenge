package com.coding.task.paymentgatewayworkerservice.service;

import com.coding.task.common.enums.ExecutionState;
import org.bson.types.ObjectId;

public interface ExecutionStateUpdatingService {

    void updateExecutionState(ObjectId id, ExecutionState newState);

}
