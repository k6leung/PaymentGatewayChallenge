package com.coding.task.paymentgatewayapi.controller;

import com.coding.task.paymentgatewayapi.model.response.IncomingPaymentAcknowledgementResponse;
import com.coding.task.paymentgatewayapi.service.PaymentTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentTransactionRestControllerTest {

    @MockBean
    private PaymentTransactionService service;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:incomingPaymentRequest.json")
    private Resource incomingPaymentRequestJson;

    @Value("classpath:paymentResultUpdateRequest.json")
    private Resource paymentResultUpdateRequestJson;

    @Test
    public void testCreatePaymentRecord() throws Exception {
        //arrange
        when(service.storePaymentRequest(any())).thenReturn(
                new IncomingPaymentAcknowledgementResponse("abcd", LocalDateTime.now())
        );

        //act and assert
        this.mockMvc.perform(post("/api/payment")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(StreamUtils.copyToByteArray(this.incomingPaymentRequestJson.getInputStream()))
        ).andExpect(status().isCreated())
        .andExpect(jsonPath("$.referenceId").value("abcd"));
    }

    @Test
    public void testUpdatePaymentRecord() throws Exception {
        //act and assert
        this.mockMvc.perform(patch("/api/payment")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(StreamUtils.copyToByteArray(this.paymentResultUpdateRequestJson.getInputStream()))
        ).andExpect(status().isNoContent());
    }
}
