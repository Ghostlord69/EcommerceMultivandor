package com.ecommerce.multivendor.dto.product;

import jakarta.validation.constraints.*;

public record ReviewRequest(@Min(1) @Max(5) int rating, String comment) {}
