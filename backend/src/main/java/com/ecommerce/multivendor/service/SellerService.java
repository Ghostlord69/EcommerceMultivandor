package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.domain.*;
import com.ecommerce.multivendor.dto.order.OrderResponse;
import com.ecommerce.multivendor.dto.product.ProductRequest;
import com.ecommerce.multivendor.dto.product.ProductResponse;
import com.ecommerce.multivendor.dto.seller.*;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SellerService {
	private final ShopRepository shopRepository;
	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final OrderRepository orderRepository;

	public SellerService(ShopRepository shopRepository, ProductRepository productRepository,
			CategoryRepository categoryRepository, OrderRepository orderRepository) {
		this.shopRepository = shopRepository;
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.orderRepository = orderRepository;
	}

	@Transactional
	public ShopResponse createOrUpdateShop(User seller, ShopRequest request) {
		Shop shop = shopRepository.findBySellerId(seller.getId()).orElse(Shop.builder().seller(seller).approved(false).build());
		shop.setName(request.name());
		shop.setDescription(request.description());
		shopRepository.save(shop);
		return toShop(shop);
	}

	@Transactional(readOnly = true)
	public ShopResponse myShop(User seller) {
		return shopRepository.findBySellerId(seller.getId()).map(this::toShop)
				.orElseThrow(() -> new ApiException("Shop not found", HttpStatus.NOT_FOUND));
	}

	private Shop requireApprovedShop(User seller) {
		Shop shop = shopRepository.findBySellerId(seller.getId())
				.orElseThrow(() -> new ApiException("Create a shop first", HttpStatus.BAD_REQUEST));
		if (!shop.isApproved()) {
			throw new ApiException("Shop awaiting admin approval", HttpStatus.FORBIDDEN);
		}
		return shop;
	}

	@Transactional
	public ProductResponse createProduct(User seller, ProductRequest request) {
		Shop shop = requireApprovedShop(seller);
		Category category = categoryRepository.findById(request.categoryId())
				.orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
		Product product = productRepository.save(Product.builder()
				.shop(shop)
				.category(category)
				.name(request.name())
				.description(request.description())
				.price(request.price())
				.stock(request.stock())
				.imageUrl(request.imageUrl())
				.active(true)
				.build());
		return ProductMapper.toResponse(product);
	}

	@Transactional
	public ProductResponse updateProduct(User seller, Long productId, ProductRequest request) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
		if (!product.getShop().getSeller().getId().equals(seller.getId())) {
			throw new ApiException("Product not found", HttpStatus.NOT_FOUND);
		}
		Category category = categoryRepository.findById(request.categoryId())
				.orElseThrow(() -> new ApiException("Category not found", HttpStatus.NOT_FOUND));
		product.setName(request.name());
		product.setDescription(request.description());
		product.setPrice(request.price());
		product.setStock(request.stock());
		product.setImageUrl(request.imageUrl());
		product.setCategory(category);
		return ProductMapper.toResponse(product);
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> myProducts(User seller) {
		Shop shop = shopRepository.findBySellerId(seller.getId())
				.orElseThrow(() -> new ApiException("Shop not found", HttpStatus.NOT_FOUND));
		return productRepository.findByShopId(shop.getId()).stream().map(ProductMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> myOrders(User seller, OrderService orderService) {
		return orderRepository.findByItemsShopSellerIdOrderByCreatedAtDesc(seller.getId()).stream()
				.map(orderService::toOrder)
				.toList();
	}

	@Transactional(readOnly = true)
	public SellerDashboardResponse dashboard(User seller) {
		List<OrderEntity> orders = orderRepository.findByItemsShopSellerIdOrderByCreatedAtDesc(seller.getId());
		long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.PENDING).count();
		long lowStock = productRepository.findByShopSellerIdAndStockLessThanEqual(seller.getId(), 5).size();
		BigDecimal earnings = orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.SHIPPED || o.getStatus() == OrderStatus.DELIVERED)
				.flatMap(o -> o.getItems().stream())
				.filter(i -> i.getShop().getSeller().getId().equals(seller.getId()))
				.map(OrderItem::getLineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		Map<String, Long> byDay = orders.stream().collect(Collectors.groupingBy(
				o -> LocalDate.ofInstant(o.getCreatedAt(), ZoneOffset.UTC).toString(), Collectors.counting()));
		List<SellerDashboardResponse.NamedCount> series = byDay.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(e -> new SellerDashboardResponse.NamedCount(e.getKey(), e.getValue()))
				.toList();
		return new SellerDashboardResponse(pending, lowStock, earnings, series);
	}

	private ShopResponse toShop(Shop shop) {
		return new ShopResponse(shop.getId(), shop.getName(), shop.getDescription(), shop.isApproved(),
				shop.getSeller().getId(), shop.getSeller().getFullName());
	}
}
