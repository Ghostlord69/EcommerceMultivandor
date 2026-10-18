package com.ecommerce.multivendor.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(Long productId, String productName, Long shopId, String shopName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
