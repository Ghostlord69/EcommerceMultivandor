package com.ecommerce.multivendor.dto.admin;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(
		@NotBlank String code,
		@NotNull @DecimalMin("1") @DecimalMax("90") BigDecimal discountPercent,
		boolean active,
		Instant expiresAt
) {}
