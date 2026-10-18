package com.ecommerce.multivendor.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {}
