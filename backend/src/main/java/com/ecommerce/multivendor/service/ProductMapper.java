package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.domain.Product;
import com.ecommerce.multivendor.dto.product.ProductResponse;

public final class ProductMapper {
	private ProductMapper() {}

	public static ProductResponse toResponse(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getStock(),
				product.getCategory().getId(),
				product.getCategory().getName(),
				product.getShop().getId(),
				product.getShop().getName(),
				product.getImageUrl(),
				product.isActive());
	}
}
