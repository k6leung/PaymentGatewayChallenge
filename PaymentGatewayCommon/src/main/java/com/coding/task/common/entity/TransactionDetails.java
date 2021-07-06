package com.coding.task.common.entity;

import com.coding.task.common.enums.Currency;
import com.coding.task.common.enums.PaymentMethod;
import com.coding.task.common.enums.PaymentResult;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//for simplicity:
//assuming only 1 payment method
//assuming only HKD supported
//assuming valid account number size is between 10 to 16
//assuming no locale
@Data
@NoArgsConstructor
public class TransactionDetails {

    private String debitAccountNumber;

    private String creditAccountNumber;

    private BigDecimal amount;

    private Currency currency;

    private PaymentMethod paymentMethod;

    private PaymentResult paymentResult;

    private LocalDateTime receiveTime;

    private LocalDateTime transactionCompleteTime;
}
