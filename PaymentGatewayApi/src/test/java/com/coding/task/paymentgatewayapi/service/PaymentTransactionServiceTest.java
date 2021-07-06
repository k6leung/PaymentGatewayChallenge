package com.coding.task.paymentgatewayapi.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.common.enums.PaymentResult;
import com.coding.task.paymentgatewayapi.model.request.IncomingPaymentRequest;
import com.coding.task.paymentgatewayapi.model.request.PaymentResultUpdateRequest;
import com.coding.task.paymentgatewayapi.model.response.IncomingPaymentAcknowledgementResponse;
import com.coding.task.paymentgatewayapi.repository.ApiPaymentTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@DataMongoTest
@ComponentScan(basePackages = "com.coding.task.paymentgatewayapi")
public class PaymentTransactionServiceTest {

    @Autowired
    private ApiPaymentTransactionRepository repository;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private PaymentTransactionService service;

    @Value("classpath:incomingPaymentRequest.json")
    private Resource incomingPaymentRequestJson;

    @Value("classpath:paymentTransaction.json")
    private Resource paymentTransactionJson;

    @BeforeEach
    public void init() throws Exception {
        this.repository.deleteAll();
    }

    @Test
    public void testStorePaymentRequest() throws Exception {
        //arrange
        IncomingPaymentRequest request = this.objectMapper.readValue(
                this.incomingPaymentRequestJson.getInputStream(),
                IncomingPaymentRequest.class
        );

        //act
        IncomingPaymentAcknowledgementResponse response = this.service.storePaymentRequest(request);

        //assert
        PaymentTransaction transaction = this.repository.findById(new ObjectId(response.getReferenceId()))
                .orElse(null);

        assertAll(
                () -> assertThat(transaction).isNotNull(),
                () -> assertThat(transaction.getTransactionDetails().getDebitAccountNumber())
                        .isEqualTo("1234567890"),
                () -> assertThat(transaction.getExecutionState())
                        .isEqualTo(ExecutionState.PENDING_FOR_EXECUTION),
                () -> assertThat(transaction.getTransactionDetails().getAmount())
                        .isEqualTo(new BigDecimal(100))
        );
    }

    @Test
    public void testUpdatePaymentTransaction() throws Exception {
        //arrange
        PaymentTransaction transaction = this.objectMapper.readValue(
                this.paymentTransactionJson.getInputStream(),
                PaymentTransaction.class
        );
        transaction = this.repository.save(transaction);

        LocalDateTime now = LocalDateTime.now();
        PaymentResultUpdateRequest paymentResultUpdateRequest =
                new PaymentResultUpdateRequest(
                        transaction.getId().toString(),
                        PaymentResult.SUCCESSFUL,
                        now
                );

        //act
        this.service.updatePaymentTransaction(paymentResultUpdateRequest);

        //assert
        PaymentTransaction transactionFromDb = this.repository.findById(transaction.getId())
                .orElse(null);

        assertAll(
                () -> assertThat(transactionFromDb).isNotNull(),
                () -> assertThat(transactionFromDb.getTransactionDetails().getPaymentResult())
                .isEqualTo(PaymentResult.SUCCESSFUL)
        );
    }

}
