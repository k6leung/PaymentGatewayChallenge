package com.coding.task.paymentgatewayapi.controlleradvice;

import com.coding.task.common.enums.ApiFailureReason;
import com.coding.task.common.model.response.PaymentGatewayErrorResponse;
import com.coding.task.paymentgatewayapi.exception.InvalidTransactionStateException;
import com.coding.task.paymentgatewayapi.exception.TransactionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageConversionException;

import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ErrorHandlerControllerAdviceTest {

    @Test
    public void testHandleHttpMessageConversionException() {
        //arrange
        ErrorHandlerControllerAdvice advice = new ErrorHandlerControllerAdvice();
        HttpMessageConversionException ex = new HttpMessageConversionException("test");

        //act
        PaymentGatewayErrorResponse response = advice.handleHttpMessageConversionException(ex);
        Map<String, String> errorDetails = response.getErrorDetails();

        //assert
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(errorDetails).isNotEmpty(),
                () -> assertThat(errorDetails.keySet().contains("request")).isNotNull(),
                () -> assertThat(errorDetails.get("request")).isEqualTo("test"),
                () -> assertThat(response.getReason())
                        .isEqualTo(ApiFailureReason.REQUEST_ERROR)
        );
    }

    @Test
    public void testHandleConstraintViolationException() {
        //arrange
        ErrorHandlerControllerAdvice advice = new ErrorHandlerControllerAdvice();
        ConstraintViolationException ex = new ConstraintViolationException("test", new HashSet<>());

        //act
        PaymentGatewayErrorResponse response = advice.handleConstraintViolationException(ex);

        //assert
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getReason())
                        .isEqualTo(ApiFailureReason.REQUEST_ERROR)
        );
    }

    //cannot instanciate MethodArgumentNotValidException
    /*@Test
    public void testHandleMethodArgumentNotValidException() {

    }*/

    @Test
    public void testHandleTransactionNotFoundException() {
        //arrange
        ErrorHandlerControllerAdvice advice = new ErrorHandlerControllerAdvice();
        TransactionNotFoundException ex = new TransactionNotFoundException("test", "abcd");

        //act
        PaymentGatewayErrorResponse response = advice.handleTransactionNotFoundException(ex);
        Map<String, String> errorDetails = response.getErrorDetails();

        //assert
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getReason())
                        .isEqualTo(ApiFailureReason.REQUEST_ERROR),
                () -> assertThat(errorDetails).isNotEmpty(),
                () -> assertThat(errorDetails.get("abcd")).isEqualTo("test")
        );
    }

    @Test
    public void testHandleInvalidTransactionStateException() {
        //arrange
        ErrorHandlerControllerAdvice advice = new ErrorHandlerControllerAdvice();
        InvalidTransactionStateException ex = new InvalidTransactionStateException("abcd", "1234");

        //act
        PaymentGatewayErrorResponse response = advice.handleInvalidTransactionStateException(ex);
        Map<String, String> errorDetails = response.getErrorDetails();

        //assert
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getReason())
                        .isEqualTo(ApiFailureReason.TRANSACTION_ERROR),
                () -> assertThat(errorDetails).isNotEmpty(),
                () -> assertThat(errorDetails.get("1234")).isEqualTo("Invalid state")
        );
    }

    @Test
    public void testHandleGenericException() {
        //arrange
        ErrorHandlerControllerAdvice advice = new ErrorHandlerControllerAdvice();
        RuntimeException ex = new RuntimeException("test");

        //act
        PaymentGatewayErrorResponse response = advice.handleGenericException(ex);
        Map<String, String> errorDetails = response.getErrorDetails();

        //assert
        assertAll(
                () -> assertThat(response).isNotNull(),
                () -> assertThat(response.getReason())
                        .isEqualTo(ApiFailureReason.SYSTEM_ERROR),
                () -> assertThat(errorDetails).isNotEmpty(),
                () -> assertThat(errorDetails.get("runtime")).isEqualTo("Generic error")
        );
    }
}
