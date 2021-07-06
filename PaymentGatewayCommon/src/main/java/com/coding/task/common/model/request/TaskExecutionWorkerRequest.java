package com.coding.task.common.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


//internal api request class, should contain only valid data so workers can skip validation
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionWorkerRequest {

    private String transactionId;

}
