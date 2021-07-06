package com.coding.task.paymentgatewayapi.service;

import com.coding.task.paymentgatewayapi.model.request.IncomingPaymentRequest;
import com.coding.task.paymentgatewayapi.model.request.PaymentResultUpdateRequest;
import com.coding.task.paymentgatewayapi.model.response.IncomingPaymentAcknowledgementResponse;

public interface PaymentTransactionService {

    IncomingPaymentAcknowledgementResponse storePaymentRequest(IncomingPaymentRequest request);

    void updatePaymentTransaction(PaymentResultUpdateRequest request);
}
