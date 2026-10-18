package com.ecommerce.multivendor.dto.auth;

import com.ecommerce.multivendor.domain.Role;

public record AuthResponse(String token, Long userId, String email, String fullName, Role role) {}
