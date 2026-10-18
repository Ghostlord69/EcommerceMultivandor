package com.ecommerce.multivendor.dto.order;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(@NotNull Long addressId, String couponCode) {}
