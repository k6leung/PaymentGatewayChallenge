package com.coding.task.paymentgatewaytaskdispatcher.service;

import com.coding.task.common.enums.ExecutionState;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
public class PaymentServiceTest {

    @MockBean
    private UpdateExecutionStateService updateExecutionStateService;

    @Autowired
    private PaymentService service;

    @Captor
    private ArgumentCaptor<ExecutionState> argumentCaptor;

    @Test
    public void testDispatchPaymentSuccessful() throws Exception {
        //arrange
        stubFor(post("/api/payment")
                .willReturn(aResponse().withStatus(201)));

        ObjectId objectId = new ObjectId("60e41ee4a553e1010c5896b0");

        //act
        this.service.dispatchPayment(objectId);

        //assert
        verify(this.updateExecutionStateService, times(0))
                .updateExecutionState(any(), any());
    }

    @Test
    public void testDispatchPaymentFail() throws Exception {
        stubFor(post("/api/payment")
                .willReturn(aResponse().withStatus(500)));

        ObjectId objectId = new ObjectId("60e41ee4a553e1010c5896b0");

        //act
        this.service.dispatchPayment(objectId);

        //assert
        verify(this.updateExecutionStateService, times(1))
                .updateExecutionState(any(), this.argumentCaptor.capture());
        assertThat(this.argumentCaptor.getValue())
                .isEqualTo(ExecutionState.PAYMENT_REQUEST_TECHNICAL_FAILED);
    }
}
