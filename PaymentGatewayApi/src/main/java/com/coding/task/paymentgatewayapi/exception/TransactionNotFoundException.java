package com.coding.task.paymentgatewayapi.exception;

import lombok.Getter;

public class TransactionNotFoundException extends RuntimeException {

    @Getter
    private String transactionId;

    public TransactionNotFoundException(String message,
                                        String transactionId) {
        super(message);

        this.transactionId = transactionId;
    }

    public TransactionNotFoundException(String message,
                                        Throwable cause,
                                        String transactionId) {
        super(message, cause);

        this.transactionId = transactionId;
    }
}
