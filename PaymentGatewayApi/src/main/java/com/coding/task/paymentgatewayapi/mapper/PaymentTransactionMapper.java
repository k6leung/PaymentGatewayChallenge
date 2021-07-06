package com.coding.task.paymentgatewayapi.mapper;

import com.coding.task.common.entity.PaymentTransaction;
import com.coding.task.paymentgatewayapi.model.request.IncomingPaymentRequest;
import com.coding.task.paymentgatewayapi.model.request.PaymentResultUpdateRequest;
import org.mapstruct.*;

@Mapper
public interface PaymentTransactionMapper {

    @BeanMapping(nullValuePropertyMappingStrategy= NullValuePropertyMappingStrategy.IGNORE,
        nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL
    )
    @Mappings({
            @Mapping(source = "request.debitAccountNumber", target = "transactionDetails.debitAccountNumber"),
            @Mapping(source = "request.creditAccountNumber", target = "transactionDetails.creditAccountNumber"),
            @Mapping(source = "request.amount", target = "transactionDetails.amount"),
            @Mapping(source = "request.currency", target = "transactionDetails.currency"),
            @Mapping(source = "request.paymentMethod", target = "transactionDetails.paymentMethod"),
            @Mapping(source = "request.callbackUri", target = "callbackUri")
    })
    PaymentTransaction mapTransactionRequestForPost(IncomingPaymentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy= NullValuePropertyMappingStrategy.IGNORE,
            nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL
    )
    @Mappings({
            @Mapping(target="transaction.id", ignore = true),
            @Mapping(source = "request.paymentResult", target = "transaction.transactionDetails.paymentResult"),
            @Mapping(source = "request.paymentCompleteTime", target = "transaction.transactionDetails.transactionCompleteTime")
    })
    void mapTransactionUpdateForPatch(PaymentResultUpdateRequest request, @MappingTarget PaymentTransaction transaction);
}
