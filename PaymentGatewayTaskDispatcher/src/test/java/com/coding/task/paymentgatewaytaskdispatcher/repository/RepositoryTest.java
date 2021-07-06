package com.coding.task.paymentgatewaytaskdispatcher.repository;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.paymentgatewaytaskdispatcher.client.WorkerServiceClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataMongoTest
@ActiveProfiles("test")
@ComponentScan(basePackages = {"com.coding.task.paymentgatewaytaskdispatcher"})
public class RepositoryTest {

    @Autowired
    private DispatcherPaymentTransactionRepositoryDispatchClaim repository;

    @MockBean
    private WorkerServiceClient client;

    @Value("classpath:claimPaymentAndDispatch.json")
    private Resource claimPaymentAndDispatchJson;

    @Value("classpath:findTransactionForPaymentTest.json")
    private Resource findTransactionForPaymentTestJson;

    @Value("classpath:findTransactionForNotificationTest.json")
    private Resource findTransactionForNotificationTestJson;

    @Value("classpath:findTransactionForPaymentUnclaimTest.json")
    private Resource findTransactionForPaymentUnclaimTestJson;

    @Value("classpath:findTransactionForNotificationUnclaimTest.json")
    private Resource findTransactionForNotificationUnclaimTestJson;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    public void init() throws Exception {
        this.repository.deleteAll();
    }

    @Test
    public void testClaimTransactionForDispatch() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.claimPaymentAndDispatchJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);
        ObjectId[] objectIds = paymentTransactionList.stream()
                .map(transaction -> transaction.getId())
                .toArray(ObjectId[]::new);

        LocalDateTime now = LocalDateTime.now();

        //act
        this.repository.claimTransactionForDispatch(
                ExecutionState.PAYMENT_EXECUTION_CLAIMED,
                "paymentDispatchClaimTime",
                now,
                objectIds
        );

        //assert
        paymentTransactionList = this.repository.findAll();

        assertThat(paymentTransactionList.size()).isEqualTo(4);
        for(PaymentTransaction transaction : paymentTransactionList) {
            assertThat(transaction.getExecutionState())
                    .isEqualTo(ExecutionState.PAYMENT_EXECUTION_CLAIMED);
        }
    }

    @Test
    public void testUnclaimTransaction() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.claimPaymentAndDispatchJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);
        ObjectId[] objectIds = paymentTransactionList.stream()
                .map(transaction -> transaction.getId())
                .toArray(ObjectId[]::new);

        //act
        this.repository.unclaimTransaction(
                ExecutionState.PENDING_FOR_EXECUTION,
                objectIds
        );

        //assert
        paymentTransactionList = this.repository.findAll();

        assertThat(paymentTransactionList.size()).isEqualTo(4);
        for(PaymentTransaction transaction : paymentTransactionList) {
            assertThat(transaction.getExecutionState())
                    .isEqualTo(ExecutionState.PENDING_FOR_EXECUTION);
        }
    }

    @Test
    public void testFindTransactionForPayment() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.findTransactionForPaymentTestJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);

        //act
        LocalDateTime now = LocalDateTime.now();
        paymentTransactionList = this.repository.findTransactionForPayment(
                6,
                now,
                now,
                PageRequest.of(0, 100));

        //assert
        assertThat(paymentTransactionList).isNotNull();
        assertThat(paymentTransactionList.size()).isEqualTo(3);
    }

    @Test
    public void testFindTransactionForNotification() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.findTransactionForNotificationTestJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);

        //act
        LocalDateTime now = LocalDateTime.now();
        paymentTransactionList = this.repository.findTransactionForNotification(
                6,
                now,
                now,
                PageRequest.of(0, 100));

        //assert
        assertThat(paymentTransactionList).isNotNull();
        assertThat(paymentTransactionList.size()).isEqualTo(3);
    }

    @Test
    public void testFindTransactionForPaymentUnclaim() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.findTransactionForPaymentUnclaimTestJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);

        //act
        LocalDateTime now = LocalDateTime.now();
        paymentTransactionList = this.repository.findTransactionForPaymentUnclaim(
                now,
                PageRequest.of(0, 100)
        );

        //assert
        assertThat(paymentTransactionList).isNotNull();
        assertThat(paymentTransactionList.size()).isEqualTo(1);
    }

    @Test
    public void testFindTransactionForNotificationUnclaim() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.findTransactionForNotificationUnclaimTestJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);

        //act
        LocalDateTime now = LocalDateTime.now();
        paymentTransactionList = this.repository.findTransactionForNotificationUnclaim(
                now,
                PageRequest.of(0, 100)
        );

        //assert
        assertThat(paymentTransactionList).isNotNull();
        assertThat(paymentTransactionList.size()).isEqualTo(1);
    }

}
