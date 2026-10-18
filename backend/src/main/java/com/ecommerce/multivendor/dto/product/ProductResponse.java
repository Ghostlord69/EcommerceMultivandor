package com.ecommerce.multivendor.dto.product;

import java.math.BigDecimal;

public record ProductResponse(
		Long id, String name, String description, BigDecimal price, int stock,
		Long categoryId, String categoryName, Long shopId, String shopName, String imageUrl, boolean active
) {}
