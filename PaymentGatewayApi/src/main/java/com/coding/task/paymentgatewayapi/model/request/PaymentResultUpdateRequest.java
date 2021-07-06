package com.coding.task.paymentgatewayapi.model.request;

import com.coding.task.common.enums.PaymentResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

//for simplicity
//assume protocol agreed, possible values are same as enum PaymentResult
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultUpdateRequest {

    @NotNull(message = "id cannot be null")
    @NotEmpty(message = "id cannot be empty")
    private String id;

    @NotNull(message = "paymentResult cannot be null")
    private PaymentResult paymentResult;

    @NotNull(message = "paymentCompleteTime cannot be null")
    private LocalDateTime paymentCompleteTime;

}
