package com.ecommerce.multivendor.controller;

import com.ecommerce.multivendor.dto.cart.*;
import com.ecommerce.multivendor.security.AuthenticatedUser;
import com.ecommerce.multivendor.service.CartService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping
	public CartResponse get(@AuthenticationPrincipal AuthenticatedUser user) {
		return cartService.getCart(user.getUser());
	}

	@PostMapping("/items")
	public CartResponse add(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CartItemRequest request) {
		return cartService.addItem(user.getUser(), request);
	}

	@PutMapping("/items/{productId}")
	public CartResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productId,
			@RequestParam int quantity) {
		return cartService.updateItem(user.getUser(), productId, quantity);
	}
}
