package com.coding.task.paymentgatewayapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class IncomingPaymentAcknowledgementResponse {

    private String referenceId;

    private LocalDateTime acknowledgeTime;

}
