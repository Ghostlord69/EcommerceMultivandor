package com.ecommerce.multivendor.controller;

import com.ecommerce.multivendor.dto.order.*;
import com.ecommerce.multivendor.security.AuthenticatedUser;
import com.ecommerce.multivendor.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/addresses")
	public List<AddressResponse> addresses(@AuthenticationPrincipal AuthenticatedUser user) {
		return orderService.listAddresses(user.getUser());
	}

	@PostMapping("/addresses")
	public AddressResponse addAddress(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody AddressRequest request) {
		return orderService.addAddress(user.getUser(), request);
	}

	@PostMapping("/checkout")
	public OrderResponse checkout(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CheckoutRequest request) {
		return orderService.checkout(user.getUser(), request);
	}

	@GetMapping("/orders")
	public List<OrderResponse> orders(@AuthenticationPrincipal AuthenticatedUser user) {
		return orderService.myOrders(user.getUser());
	}

	@GetMapping("/orders/{id}")
	public OrderResponse order(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
		return orderService.getOrderForCustomer(user.getUser(), id);
	}

	@PostMapping("/orders/{id}/cancel")
	public OrderResponse cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
		return orderService.cancelOrder(user.getUser(), id);
	}
}
