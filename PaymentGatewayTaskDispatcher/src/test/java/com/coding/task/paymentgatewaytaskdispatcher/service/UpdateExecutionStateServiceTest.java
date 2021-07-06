package com.coding.task.paymentgatewaytaskdispatcher.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.paymentgatewaytaskdispatcher.client.WorkerServiceClient;
import com.coding.task.paymentgatewaytaskdispatcher.repository.DispatcherPaymentTransactionRepositoryDispatchClaim;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataMongoTest
@ActiveProfiles("test")
@ComponentScan(basePackages = {"com.coding.task.paymentgatewaytaskdispatcher"})
public class UpdateExecutionStateServiceTest {

    //cannot use spy on spring data repositories...
    @Autowired
    private DispatcherPaymentTransactionRepositoryDispatchClaim repository;

    @Autowired
    private UpdateExecutionStateService service;

    @MockBean
    private WorkerServiceClient client;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Value("classpath:paymentTransaction.json")
    private Resource paymentTransactionJson;

    @BeforeEach
    public void init() throws Exception {
        this.repository.deleteAll();
    }

    @Test
    public void testNoChangeToDb() {
        //arrange
        ObjectId id = new ObjectId("60e41ee4a553e1010c5896b0");

        //act
        service.updateExecutionState(id, ExecutionState.RESPONSE_SENDING_EXECUTION_CLAIMED);

        //assert
        List<PaymentTransaction> dataFromDb = this.repository.findAll();
        assertThat(dataFromDb.size()).isEqualTo(0);
    }

    @Test
    public void testRecordUpdated() throws Exception {
        //arrange
        PaymentTransaction transaction = this.objectMapper.readValue(
                this.paymentTransactionJson.getInputStream(),
                PaymentTransaction.class
        );

        transaction = this.repository.save(transaction);

        //act
        this.service.updateExecutionState(transaction.getId(),
                ExecutionState.RESULT_RECEIVED);

        //assert
        transaction = this.repository.findById(transaction.getId()).orElseThrow(
                () -> new NullPointerException("no record found")
        );

        assertThat(transaction).isNotNull();
        assertThat(transaction.getExecutionState()).isEqualTo(ExecutionState.RESULT_RECEIVED);
    }
}
