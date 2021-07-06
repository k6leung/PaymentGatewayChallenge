package com.coding.task.paymentgatewaytaskdispatcher.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.paymentgatewaytaskdispatcher.client.WorkerServiceClient;
import com.coding.task.paymentgatewaytaskdispatcher.repository.DispatcherPaymentTransactionRepositoryDispatchClaim;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@DataMongoTest
@ActiveProfiles("test")
@ComponentScan(basePackages = {"com.coding.task.paymentgatewaytaskdispatcher"})
public class ScheduledJobServiceTest {

    @Autowired
    private ScheduledJobService service;

    @Autowired
    private DispatcherPaymentTransactionRepositoryDispatchClaim repository;

    @MockBean
    private WorkerServiceClient client;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private NotificationService notificationService;

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
    public void testRunPaymentJob() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.findTransactionForPaymentTestJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);

        //act
        this.service.runPaymentJob();

        //assert
        verify(this.paymentService, times(3))
                .dispatchPayment(any());
    }

    @Test
    public void testRunNotificationJob() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.findTransactionForNotificationTestJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);

        //act
        this.service.runNotificationJob();

        //assert
        verify(this.notificationService, times(3))
                .dispatchNotification(any());
    }

    @Test
    public void testExpireClaim() throws Exception {
        //arrange
        List<PaymentTransaction> paymentTransactionList = this.objectMapper.readValue(
                this.findTransactionForPaymentUnclaimTestJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);

        paymentTransactionList = this.objectMapper.readValue(
                this.findTransactionForNotificationUnclaimTestJson.getInputStream(),
                new TypeReference<List<PaymentTransaction>>() {}
        );

        paymentTransactionList = this.repository.insert(paymentTransactionList);

        //act
        this.service.expireClaim();

        //assert
        paymentTransactionList = this.repository.findAll();

        List<PaymentTransaction> pendForExecutionList = paymentTransactionList.stream()
                .filter(transaction -> transaction.getExecutionState() == ExecutionState.PENDING_FOR_EXECUTION)
                .collect(Collectors.toList());

        List<PaymentTransaction> resultReceivedList = paymentTransactionList.stream()
                .filter(transaction -> transaction.getExecutionState() == ExecutionState.RESULT_RECEIVED)
                .collect(Collectors.toList());

        assertThat(pendForExecutionList.size()).isEqualTo(1);
        assertThat(resultReceivedList.size()).isEqualTo(1);
    }
}
