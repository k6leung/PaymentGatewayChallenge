package com.coding.task.common.model.response;

import com.coding.task.common.enums.ApiFailureReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayErrorResponse {

    private ApiFailureReason reason;

    private Map<String, String> errorDetails;

}
