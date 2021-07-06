package com.coding.task.paymentgatewayworkerservice.controller;

import com.coding.task.paymentgatewayworkerservice.service.PaymentExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StreamUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentWorkerRestControllerTest {

    @MockBean
    private PaymentExecutionService service;

    @Autowired
    private MockMvc mockMvc;

    @Value("classpath:taskExecutionWorkerRequest.json")
    private Resource taskExecutionWorkerRequestJson;

    @Test
    public void testAcknowledgePaymentAndWork() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/payment")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(StreamUtils.copyToByteArray(this.taskExecutionWorkerRequestJson.getInputStream()))
        ).andExpect(status().isCreated());
    }
}
