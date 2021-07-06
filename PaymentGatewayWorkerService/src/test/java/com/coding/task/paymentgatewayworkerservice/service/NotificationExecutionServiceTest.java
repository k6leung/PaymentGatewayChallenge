package com.coding.task.paymentgatewayworkerservice.service;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.common.enums.ExecutionState;
import com.coding.task.common.model.request.TaskExecutionWorkerRequest;
import com.coding.task.paymentgatewayworkerservice.client.CustomerNotificationClient;
import com.coding.task.paymentgatewayworkerservice.client.RemotePaymentServiceClient;
import com.coding.task.paymentgatewayworkerservice.repository.WorkerPaymentTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationExecutionServiceTest {

    @MockBean
    private ExecutionStateUpdatingService executionStateUpdatingService;

    @MockBean
    private WorkerPaymentTransactionRepository repository;

    @MockBean
    private RemotePaymentServiceClient remotePaymentServiceClient;

    @MockBean
    private CustomerNotificationClient client;

    @Autowired
    private NotificationExecutionService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:paymentTransaction.json")
    private Resource paymentTransactionJson;

    @Captor
    private ArgumentCaptor<ExecutionState> executionStateArgumentCaptor;

    @Test
    public void testExecutNotificationSucceed() throws Exception {
        //arrange
        PaymentTransaction transaction = this.objectMapper.readValue(
                this.paymentTransactionJson.getInputStream(),
                PaymentTransaction.class
        );
        when(this.repository.findById(any())).thenReturn(Optional.of(transaction));

        //act
        this.service.executeNotification(new TaskExecutionWorkerRequest("60e41ee4a553e1010c5896b0"));

        //assert
        assertThat(transaction.getNotificationSendingAttempts()).isEqualTo(1);
        verify(this.repository, times(1)).save(any());
        verify(this.client, times(1)).notifyCustomer(any());
    }

    @Test
    public void testRuntimeExceptionThrown() throws Exception {
        //arrange
        PaymentTransaction transaction = this.objectMapper.readValue(
                this.paymentTransactionJson.getInputStream(),
                PaymentTransaction.class
        );
        when(this.repository.findById(any())).thenReturn(Optional.of(transaction));
        when(this.repository.save(any())).thenThrow(
                new RuntimeException("test fail")
        );

        //act
        this.service.executeNotification(new TaskExecutionWorkerRequest("60e41ee4a553e1010c5896b0"));

        //assert
        verify(this.executionStateUpdatingService, times(1))
                .updateExecutionState(any(), this.executionStateArgumentCaptor.capture());
        assertThat(this.executionStateArgumentCaptor.getValue())
                .isEqualTo(ExecutionState.RESPONSE_SENDING_TECHNICAL_FAILED);
    }

    @Test
    public void testFailWithResourceAccessExceptionNotTimeout() throws Exception {
        //arrange
        PaymentTransaction transaction = this.objectMapper.readValue(
                this.paymentTransactionJson.getInputStream(),
                PaymentTransaction.class
        );
        when(this.repository.findById(any())).thenReturn(Optional.of(transaction));

        IOException ioe = new IOException("test fail");
        ResourceAccessException rae = new ResourceAccessException("test fail", ioe);
        doThrow(rae).when(this.client).notifyCustomer(any());

        //act
        this.service.executeNotification(new TaskExecutionWorkerRequest("60e41ee4a553e1010c5896b0"));

        //assert
        verify(this.executionStateUpdatingService).updateExecutionState(any(),
                this.executionStateArgumentCaptor.capture());
        assertThat(this.executionStateArgumentCaptor.getValue())
                .isEqualTo(ExecutionState.RESPONSE_SENDING_TECHNICAL_FAILED);
    }

    @Test
    public void testFailWithResourceAccessExceptionTimeout() throws Exception {
        //arrange
        PaymentTransaction transaction = this.objectMapper.readValue(
                this.paymentTransactionJson.getInputStream(),
                PaymentTransaction.class
        );
        when(this.repository.findById(any())).thenReturn(Optional.of(transaction));

        SocketTimeoutException ste = new SocketTimeoutException("test fail");
        ResourceAccessException rae = new ResourceAccessException("test fail", ste);

        doThrow(rae).when(this.client).notifyCustomer(any());

        //act
        this.service.executeNotification(new TaskExecutionWorkerRequest("60e41ee4a553e1010c5896b0"));

        //assert
        verify(this.executionStateUpdatingService).updateExecutionState(any(),
                this.executionStateArgumentCaptor.capture());
        assertThat(this.executionStateArgumentCaptor.getValue())
                .isEqualTo(ExecutionState.RESPONSE_SENDING_TIMEOUT);
    }

    @Test
    public void testFailWithRestClientResponseException4xxError() throws Exception {
        //arrange
        PaymentTransaction transaction = this.objectMapper.readValue(
                this.paymentTransactionJson.getInputStream(),
                PaymentTransaction.class
        );
        when(this.repository.findById(any())).thenReturn(Optional.of(transaction));

        RestClientResponseException e =
                new RestClientResponseException(
                        "test fail",
                        HttpStatus.CONFLICT.value(),
                        "test fail",
                        new HttpHeaders(),
                        new byte[]{},
                        Charset.defaultCharset());
        doThrow(e).when(this.client).notifyCustomer(any());

        //act
        this.service.executeNotification(new TaskExecutionWorkerRequest("60e41ee4a553e1010c5896b0"));

        //assert
        verify(this.executionStateUpdatingService).updateExecutionState(any(),
                this.executionStateArgumentCaptor.capture());
        assertThat(this.executionStateArgumentCaptor.getValue())
                .isEqualTo(ExecutionState.RESPONSE_SENDING_UNRECOVERABLE_FAILED);
    }

    @Test
    public void testFailWithRestClientResponseException5xxError() throws Exception {
        //arrange
        PaymentTransaction transaction = this.objectMapper.readValue(
                this.paymentTransactionJson.getInputStream(),
                PaymentTransaction.class
        );
        when(this.repository.findById(any())).thenReturn(Optional.of(transaction));

        RestClientResponseException e =
                new RestClientResponseException(
                        "test fail",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "test fail",
                        new HttpHeaders(),
                        new byte[]{},
                        Charset.defaultCharset());
        doThrow(e).when(this.client).notifyCustomer(any());

        //act
        this.service.executeNotification(new TaskExecutionWorkerRequest("60e41ee4a553e1010c5896b0"));

        //assert
        verify(this.executionStateUpdatingService).updateExecutionState(any(),
                this.executionStateArgumentCaptor.capture());
        assertThat(this.executionStateArgumentCaptor.getValue())
                .isEqualTo(ExecutionState.RESPONSE_SENDING_TECHNICAL_FAILED);
    }
}
