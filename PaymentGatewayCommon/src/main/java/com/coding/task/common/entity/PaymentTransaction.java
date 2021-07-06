package com.coding.task.common.entity;

import com.coding.task.common.enums.ExecutionState;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

//for simplicity:
//assuming only 1 payment method
//assuming only HKD supported
//assuming valid account number size is between 10 to 16
//assuming no locale
@Data
@NoArgsConstructor
@Document(collection = "PaymentTransaction")
public class PaymentTransaction {

    @Id
    private ObjectId id;
    
    private TransactionDetails transactionDetails;

    private ExecutionState executionState;

    private Integer paymentAttempts;

    private Integer notificationSendingAttempts;

    private String callbackUri;
    
    private LocalDateTime paymentDispatchClaimTime;

    private LocalDateTime notificationDispatchClaimTime;

    private LocalDateTime paymentExecutionStartTime;

    private LocalDateTime notificationExecutionStartTime;

    @Version
    private Long version;
}
