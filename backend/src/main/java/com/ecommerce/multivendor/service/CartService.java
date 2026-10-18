package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.domain.*;
import com.ecommerce.multivendor.dto.cart.*;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.CartItemRepository;
import com.ecommerce.multivendor.repository.CartRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final CatalogService catalogService;

	public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, CatalogService catalogService) {
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.catalogService = catalogService;
	}

	@Transactional
	public Cart getOrCreateCart(User user) {
		return cartRepository.findByUserId(user.getId()).orElseGet(() -> cartRepository.save(Cart.builder().user(user).items(new ArrayList<>()).build()));
	}

	@Transactional(readOnly = true)
	public CartResponse getCart(User user) {
		Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
		if (cart == null || cart.getItems().isEmpty()) {
			return new CartResponse(List.of(), BigDecimal.ZERO, 0);
		}
		return toResponse(cart);
	}

	@Transactional
	public CartResponse addItem(User user, CartItemRequest request) {
		Product product = catalogService.requireActiveProduct(request.productId());
		if (product.getStock() < request.quantity()) {
			throw new ApiException("Insufficient stock", HttpStatus.BAD_REQUEST);
		}
		Cart cart = getOrCreateCart(user);
		CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
				.orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());
		item.setQuantity(item.getQuantity() + request.quantity());
		if (item.getQuantity() > product.getStock()) {
			throw new ApiException("Insufficient stock", HttpStatus.BAD_REQUEST);
		}
		if (item.getId() == null) {
			cart.getItems().add(item);
		}
		cartItemRepository.save(item);
		return toResponse(cartRepository.findById(cart.getId()).orElseThrow());
	}

	@Transactional
	public CartResponse updateItem(User user, Long productId, int quantity) {
		Cart cart = getOrCreateCart(user);
		CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
				.orElseThrow(() -> new ApiException("Item not in cart", HttpStatus.NOT_FOUND));
		if (quantity <= 0) {
			cart.getItems().remove(item);
			cartItemRepository.delete(item);
		} else {
			if (quantity > item.getProduct().getStock()) {
				throw new ApiException("Insufficient stock", HttpStatus.BAD_REQUEST);
			}
			item.setQuantity(quantity);
			cartItemRepository.save(item);
		}
		return getCart(user);
	}

	@Transactional
	public void clearCart(User user) {
		cartRepository.findByUserId(user.getId()).ifPresent(cart -> {
			cart.getItems().clear();
			cartRepository.save(cart);
		});
	}

	private CartResponse toResponse(Cart cart) {
		List<CartItemResponse> items = cart.getItems().stream().map(i -> {
			BigDecimal line = i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
			return new CartItemResponse(i.getProduct().getId(), i.getProduct().getName(), i.getProduct().getPrice(), i.getQuantity(), line);
		}).toList();
		BigDecimal total = items.stream().map(CartItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		int count = items.stream().mapToInt(CartItemResponse::quantity).sum();
		return new CartResponse(items, total, count);
	}
}
