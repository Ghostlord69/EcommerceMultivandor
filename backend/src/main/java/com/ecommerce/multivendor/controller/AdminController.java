package com.ecommerce.multivendor.controller;

import com.ecommerce.multivendor.domain.Coupon;
import com.ecommerce.multivendor.dto.admin.*;
import com.ecommerce.multivendor.dto.common.MessageResponse;
import com.ecommerce.multivendor.dto.product.CategoryRequest;
import com.ecommerce.multivendor.dto.product.CategoryResponse;
import com.ecommerce.multivendor.dto.seller.ShopResponse;
import com.ecommerce.multivendor.service.AdminService;
import com.ecommerce.multivendor.service.CatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
	private final AdminService adminService;
	private final CatalogService catalogService;

	public AdminController(AdminService adminService, CatalogService catalogService) {
		this.adminService = adminService;
		this.catalogService = catalogService;
	}

	@GetMapping("/dashboard")
	public AdminDashboardResponse dashboard() {
		return adminService.dashboard();
	}

	@GetMapping("/shops/pending")
	public List<ShopResponse> pendingShops() {
		return adminService.pendingShops();
	}

	@PostMapping("/shops/{id}/approve")
	public MessageResponse approve(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean approved) {
		return adminService.approveShop(id, approved);
	}

	@PostMapping("/categories")
	public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
		return catalogService.createCategory(request);
	}

	@PostMapping("/coupons")
	public Coupon createCoupon(@Valid @RequestBody CouponRequest request) {
		return adminService.createCoupon(request);
	}

	@GetMapping("/coupons")
	public List<Coupon> coupons() {
		return adminService.listCoupons();
	}
}
