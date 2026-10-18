package com.ecommerce.multivendor.dto.order;

import com.ecommerce.multivendor.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
		Long id, OrderStatus status, BigDecimal subtotal, BigDecimal discount, BigDecimal total,
		String couponCode, Instant createdAt, List<OrderItemResponse> items
) {}
