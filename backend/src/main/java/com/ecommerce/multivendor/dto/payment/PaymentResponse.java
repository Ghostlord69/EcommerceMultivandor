package com.ecommerce.multivendor.dto.payment;

import com.ecommerce.multivendor.domain.PaymentProvider;
import com.ecommerce.multivendor.domain.PaymentStatus;
import java.math.BigDecimal;

public record PaymentResponse(
		Long id, Long orderId, PaymentProvider provider, PaymentStatus status, BigDecimal amount,
		String providerReference, String clientSecretOrOrderId, String publishableKey
) {}
