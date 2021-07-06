package com.coding.task.paymentgatewayapi.exception;

import lombok.Getter;

public class InvalidTransactionStateException extends RuntimeException {

    @Getter
    private String transactionId;

    public InvalidTransactionStateException(String message,
                                            String transactionId) {
        super(message);

        this.transactionId = transactionId;
    }

    public InvalidTransactionStateException(String message,
                                            Throwable cause,
                                            String transactionId) {
        super(message, cause);

        this.transactionId = transactionId;
    }
}
