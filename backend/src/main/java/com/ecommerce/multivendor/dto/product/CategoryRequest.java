package com.ecommerce.multivendor.dto.product;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name, String description) {}
