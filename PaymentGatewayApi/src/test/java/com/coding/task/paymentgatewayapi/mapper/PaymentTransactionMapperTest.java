package com.coding.task.paymentgatewayapi.mapper;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.entity.TransactionDetails;
import com.coding.task.common.enums.Currency;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.common.enums.PaymentMethod;
import com.coding.task.common.enums.PaymentResult;
import com.coding.task.paymentgatewayapi.model.request.IncomingPaymentRequest;
import com.coding.task.paymentgatewayapi.model.request.PaymentResultUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class PaymentTransactionMapperTest {

    @Autowired
    private PaymentTransactionMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:incomingPaymentRequest.json")
    private Resource incomingPaymentRequestJson;

    @Value("classpath:paymentResultUpdateRequest.json")
    private Resource paymentResultUpdateRequestJson;

    @Test
    public void testMapTransactionRequestForPost() throws Exception {
        //arrange
        IncomingPaymentRequest request = this.objectMapper.readValue(
                this.incomingPaymentRequestJson.getInputStream(),
                IncomingPaymentRequest.class
        );

        //act
        PaymentTransaction transaction = this.mapper.mapTransactionRequestForPost(request);

        //assert
        assertThat(transaction).isNotNull();

        TransactionDetails details = transaction.getTransactionDetails();
        assertAll(
                () -> assertThat(details).isNotNull(),
                () -> assertThat(details.getDebitAccountNumber())
                        .isEqualTo("1234567890"),
                () -> assertThat(details.getCreditAccountNumber())
                        .isEqualTo("0987654321"),
                () -> assertThat(details.getCurrency())
                        .isEqualTo(Currency.HKD),
                () -> assertThat(details.getPaymentMethod())
                        .isEqualTo(PaymentMethod.CREDIT_CARD),
                () -> assertThat(transaction.getCallbackUri())
                        .isEqualTo("http://www.google.com")
        );
    }

    @Test
    public void testMapTransactionUpdateForPatch() throws Exception {
        //arrange
        PaymentResultUpdateRequest request = this.objectMapper.readValue(
                this.paymentResultUpdateRequestJson.getInputStream(),
                PaymentResultUpdateRequest.class
        );

        PaymentTransaction transaction = new PaymentTransaction();
        TransactionDetails details = new TransactionDetails();
        transaction.setTransactionDetails(details);

        //act
        this.mapper.mapTransactionUpdateForPatch(request, transaction);

        //assert
        assertAll(
                () -> assertThat(details.getPaymentResult())
                        .isEqualTo(PaymentResult.SUCCESSFUL),
                () -> assertThat(details.getTransactionCompleteTime())
                        .isEqualTo(request.getPaymentCompleteTime())
        );


    }
}
