package com.ecommerce.multivendor.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(@NotNull Long paymentId, @NotBlank String providerReference) {}
