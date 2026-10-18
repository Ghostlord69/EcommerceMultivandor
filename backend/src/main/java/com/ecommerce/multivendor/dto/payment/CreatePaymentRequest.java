package com.ecommerce.multivendor.dto.payment;

import com.ecommerce.multivendor.domain.PaymentProvider;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(@NotNull Long orderId, @NotNull PaymentProvider provider) {}
