package com.ecommerce.multivendor.controller;

import com.ecommerce.multivendor.domain.OrderStatus;
import com.ecommerce.multivendor.dto.order.OrderResponse;
import com.ecommerce.multivendor.dto.product.ProductRequest;
import com.ecommerce.multivendor.dto.product.ProductResponse;
import com.ecommerce.multivendor.dto.seller.*;
import com.ecommerce.multivendor.security.AuthenticatedUser;
import com.ecommerce.multivendor.service.OrderService;
import com.ecommerce.multivendor.service.SellerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller")
public class SellerController {
	private final SellerService sellerService;
	private final OrderService orderService;

	public SellerController(SellerService sellerService, OrderService orderService) {
		this.sellerService = sellerService;
		this.orderService = orderService;
	}

	@PostMapping("/shop")
	public ShopResponse shop(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ShopRequest request) {
		return sellerService.createOrUpdateShop(user.getUser(), request);
	}

	@GetMapping("/shop")
	public ShopResponse myShop(@AuthenticationPrincipal AuthenticatedUser user) {
		return sellerService.myShop(user.getUser());
	}

	@GetMapping("/dashboard")
	public SellerDashboardResponse dashboard(@AuthenticationPrincipal AuthenticatedUser user) {
		return sellerService.dashboard(user.getUser());
	}

	@GetMapping("/products")
	public List<ProductResponse> products(@AuthenticationPrincipal AuthenticatedUser user) {
		return sellerService.myProducts(user.getUser());
	}

	@PostMapping("/products")
	public ProductResponse createProduct(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ProductRequest request) {
		return sellerService.createProduct(user.getUser(), request);
	}

	@PutMapping("/products/{id}")
	public ProductResponse updateProduct(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
			@Valid @RequestBody ProductRequest request) {
		return sellerService.updateProduct(user.getUser(), id, request);
	}

	@GetMapping("/orders")
	public List<OrderResponse> orders(@AuthenticationPrincipal AuthenticatedUser user) {
		return sellerService.myOrders(user.getUser(), orderService);
	}

	@PatchMapping("/orders/{id}/status")
	public OrderResponse status(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id, @RequestParam OrderStatus status) {
		return orderService.updateStatusForSeller(user.getUser(), id, status);
	}
}
