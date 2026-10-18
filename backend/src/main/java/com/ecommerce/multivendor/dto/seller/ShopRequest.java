package com.ecommerce.multivendor.dto.seller;

import jakarta.validation.constraints.NotBlank;

public record ShopRequest(@NotBlank String name, String description) {}
