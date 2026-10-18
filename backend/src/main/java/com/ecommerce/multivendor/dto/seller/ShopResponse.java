package com.ecommerce.multivendor.dto.seller;

public record ShopResponse(Long id, String name, String description, boolean approved, Long sellerId, String sellerName) {}
