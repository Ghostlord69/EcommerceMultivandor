package com.ecommerce.multivendor.controller;

import com.ecommerce.multivendor.dto.common.MessageResponse;
import com.ecommerce.multivendor.dto.product.ProductResponse;
import com.ecommerce.multivendor.security.AuthenticatedUser;
import com.ecommerce.multivendor.service.WishlistService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {
	private final WishlistService wishlistService;

	public WishlistController(WishlistService wishlistService) {
		this.wishlistService = wishlistService;
	}

	@GetMapping
	public List<ProductResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
		return wishlistService.list(user.getUser());
	}

	@PostMapping("/{productId}")
	public MessageResponse add(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productId) {
		return wishlistService.add(user.getUser(), productId);
	}

	@DeleteMapping("/{productId}")
	public MessageResponse remove(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productId) {
		return wishlistService.remove(user.getUser(), productId);
	}
}
