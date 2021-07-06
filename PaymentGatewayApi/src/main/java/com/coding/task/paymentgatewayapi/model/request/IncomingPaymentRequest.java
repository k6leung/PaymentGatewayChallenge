package com.coding.task.paymentgatewayapi.model.request;

import com.coding.task.common.enums.Currency;
import com.coding.task.common.enums.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;

//for simplicity:
//assuming only 1 payment method
//assuming only HKD supported
//assuming valid account number size is between 10 to 16
//assuming no locale
@Data
@NoArgsConstructor
public class IncomingPaymentRequest {

    @NotNull(message = "debitAccountNumber cannot be null")
    @NotEmpty(message = "debitAccountNumber cannot be empty")
    @Size(min = 10, max = 16, message = "Invalid debitAccountNumber length")
    private String debitAccountNumber;

    @NotNull(message = "creditAccountNumber cannot be null")
    @NotEmpty(message = "creditAccountNumber cannot be empty")
    @Size(min = 10, max = 16, message = "Invalid creditAccountNumber length")
    private String creditAccountNumber;

    @NotNull(message = "amount cannot be null")
    @DecimalMin(value = "0.1", message = "amount cannot be less than 0.1")
    private BigDecimal amount;

    @NotNull(message = "currency cannot be null")
    private Currency currency;

    @NotNull(message = "paymentMethod cannot be null")
    private PaymentMethod paymentMethod;

    @NotNull(message = "callbackUri cannot be null")
    @NotEmpty(message = "callbackUri cannot be empty")
    //copied from internet
    @Pattern(regexp = "(?:^|\\s)((https?:\\/\\/)?(?:localhost|[\\w-]+(?:\\.[\\w-]+)+)(:\\d+)?(\\/\\S*)?)")
    private String callbackUri;
}
