package com.coding.task.paymentgatewayapi.controlleradvice;

import com.coding.task.common.enums.ApiFailureReason;
import com.coding.task.paymentgatewayapi.exception.InvalidTransactionStateException;
import com.coding.task.paymentgatewayapi.exception.TransactionNotFoundException;
import com.coding.task.common.model.response.PaymentGatewayErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

//for simplicity
//minimal logging
@Slf4j
@RestControllerAdvice
public class ErrorHandlerControllerAdvice {

    private PaymentGatewayErrorResponse createErrorResponse(ApiFailureReason reason,
                                                            Map<String, String> errorDetails) {
        PaymentGatewayErrorResponse response = new PaymentGatewayErrorResponse();
        response.setReason(reason);
        response.setErrorDetails(errorDetails);

        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageConversionException.class)
    public PaymentGatewayErrorResponse handleHttpMessageConversionException(
            HttpMessageConversionException ex) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("request", ex.getMessage());

        return this.createErrorResponse(ApiFailureReason.REQUEST_ERROR, errorDetails);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public PaymentGatewayErrorResponse handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errorDetails = new HashMap<>();

        //build path : error message mapping as errorDetails
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errorDetails.put(violation.getPropertyPath().toString(),
                    violation.getMessage());
        }

        return this.createErrorResponse(ApiFailureReason.REQUEST_ERROR, errorDetails);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public PaymentGatewayErrorResponse handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errorDetails = new HashMap<>();

        //build field : error message mapping as errorDetails
        for(ObjectError objectError : ex.getBindingResult().getAllErrors()) {
            FieldError fieldError = (FieldError) objectError;

            errorDetails.put(fieldError.getField(),
                    fieldError.getDefaultMessage());
        }

        return this.createErrorResponse(ApiFailureReason.REQUEST_ERROR, errorDetails);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(TransactionNotFoundException.class)
    public PaymentGatewayErrorResponse handleTransactionNotFoundException(
            TransactionNotFoundException ex) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put(ex.getTransactionId(), ex.getMessage());

        return this.createErrorResponse(ApiFailureReason.REQUEST_ERROR, errorDetails);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InvalidTransactionStateException.class)
    public PaymentGatewayErrorResponse handleInvalidTransactionStateException(
            InvalidTransactionStateException ex) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put(ex.getTransactionId(), "Invalid state");

        return this.createErrorResponse(ApiFailureReason.TRANSACTION_ERROR, errorDetails);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
            DataAccessException.class,
            RuntimeException.class,
            Exception.class
    })
    public PaymentGatewayErrorResponse handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("runtime", "Generic error");

        return this.createErrorResponse(ApiFailureReason.SYSTEM_ERROR, errorDetails);
    }
}
