package com.ecommerce.multivendor.dto.product;

import java.time.Instant;

public record ReviewResponse(Long id, Long productId, String customerName, int rating, String comment, Instant createdAt) {}
