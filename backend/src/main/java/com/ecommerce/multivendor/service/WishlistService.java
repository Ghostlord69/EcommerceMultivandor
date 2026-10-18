package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.domain.Product;
import com.ecommerce.multivendor.domain.User;
import com.ecommerce.multivendor.domain.WishlistItem;
import com.ecommerce.multivendor.dto.common.MessageResponse;
import com.ecommerce.multivendor.dto.product.ProductResponse;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.WishlistItemRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {
	private final WishlistItemRepository wishlistItemRepository;
	private final CatalogService catalogService;

	public WishlistService(WishlistItemRepository wishlistItemRepository, CatalogService catalogService) {
		this.wishlistItemRepository = wishlistItemRepository;
		this.catalogService = catalogService;
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> list(User user) {
		return wishlistItemRepository.findByUserId(user.getId()).stream()
				.map(WishlistItem::getProduct)
				.map(ProductMapper::toResponse)
				.toList();
	}

	@Transactional
	public MessageResponse add(User user, Long productId) {
		Product product = catalogService.requireActiveProduct(productId);
		if (wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
			return new MessageResponse("Already in wishlist");
		}
		wishlistItemRepository.save(WishlistItem.builder().user(user).product(product).build());
		return new MessageResponse("Added to wishlist");
	}

	@Transactional
	public MessageResponse remove(User user, Long productId) {
		if (!wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
			throw new ApiException("Not in wishlist", HttpStatus.NOT_FOUND);
		}
		wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
		return new MessageResponse("Removed from wishlist");
	}

	public boolean contains(User user, Long productId) {
		return wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId);
	}
}
