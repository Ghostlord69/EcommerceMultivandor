package com.ecommerce.multivendor.controller;

import com.ecommerce.multivendor.dto.product.*;
import com.ecommerce.multivendor.security.AuthenticatedUser;
import com.ecommerce.multivendor.service.CatalogService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
	private final CatalogService catalogService;

	public ProductController(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@GetMapping("/products")
	public List<ProductResponse> products(@RequestParam(required = false) String q,
			@RequestParam(required = false) Long categoryId) {
		return catalogService.listProducts(q, categoryId);
	}

	@GetMapping("/products/{id}")
	public ProductResponse product(@PathVariable Long id) {
		return catalogService.getProduct(id);
	}

	@GetMapping("/categories")
	public List<CategoryResponse> categories() {
		return catalogService.listCategories();
	}

	@GetMapping("/products/{id}/reviews")
	public List<ReviewResponse> reviews(@PathVariable Long id) {
		return catalogService.listReviews(id);
	}

	@PostMapping("/products/{id}/reviews")
	public ReviewResponse addReview(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
			@Valid @RequestBody ReviewRequest request) {
		return catalogService.addReview(user.getUser(), id, request);
	}
}
