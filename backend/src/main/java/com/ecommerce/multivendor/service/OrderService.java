package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.domain.*;
import com.ecommerce.multivendor.dto.order.*;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
	private final OrderRepository orderRepository;
	private final AddressRepository addressRepository;
	private final CouponRepository couponRepository;
	private final CartService cartService;
	private final CartRepository cartRepository;
	private final ProductRepository productRepository;
	private final MailNotificationService mailNotificationService;

	public OrderService(OrderRepository orderRepository, AddressRepository addressRepository, CouponRepository couponRepository,
			CartService cartService, CartRepository cartRepository, ProductRepository productRepository,
			MailNotificationService mailNotificationService) {
		this.orderRepository = orderRepository;
		this.addressRepository = addressRepository;
		this.couponRepository = couponRepository;
		this.cartService = cartService;
		this.cartRepository = cartRepository;
		this.productRepository = productRepository;
		this.mailNotificationService = mailNotificationService;
	}

	@Transactional
	public AddressResponse addAddress(User user, AddressRequest request) {
		if (request.defaultAddress()) {
			addressRepository.findByUserId(user.getId()).forEach(a -> a.setDefaultAddress(false));
		}
		Address address = addressRepository.save(Address.builder()
				.user(user)
				.line1(request.line1())
				.line2(request.line2())
				.city(request.city())
				.state(request.state())
				.postalCode(request.postalCode())
				.country(request.country())
				.defaultAddress(request.defaultAddress())
				.build());
		return toAddress(address);
	}

	@Transactional(readOnly = true)
	public List<AddressResponse> listAddresses(User user) {
		return addressRepository.findByUserId(user.getId()).stream().map(this::toAddress).toList();
	}

	@Transactional
	public OrderResponse checkout(User user, CheckoutRequest request) {
		Cart cart = cartRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ApiException("Cart is empty", HttpStatus.BAD_REQUEST));
		if (cart.getItems().isEmpty()) {
			throw new ApiException("Cart is empty", HttpStatus.BAD_REQUEST);
		}
		Address address = addressRepository.findById(request.addressId())
				.filter(a -> a.getUser().getId().equals(user.getId()))
				.orElseThrow(() -> new ApiException("Address not found", HttpStatus.NOT_FOUND));

		BigDecimal subtotal = BigDecimal.ZERO;
		List<OrderItem> orderItems = new ArrayList<>();
		OrderEntity order = OrderEntity.builder()
				.customer(user)
				.shippingAddress(address)
				.status(OrderStatus.PENDING)
				.subtotal(BigDecimal.ZERO)
				.discount(BigDecimal.ZERO)
				.total(BigDecimal.ZERO)
				.items(orderItems)
				.build();

		for (CartItem cartItem : cart.getItems()) {
			Product product = cartItem.getProduct();
			if (product.getStock() < cartItem.getQuantity()) {
				throw new ApiException("Insufficient stock for " + product.getName(), HttpStatus.BAD_REQUEST);
			}
			BigDecimal line = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
			subtotal = subtotal.add(line);
			orderItems.add(OrderItem.builder()
					.order(order)
					.product(product)
					.shop(product.getShop())
					.quantity(cartItem.getQuantity())
					.unitPrice(product.getPrice())
					.lineTotal(line)
					.build());
			product.setStock(product.getStock() - cartItem.getQuantity());
			productRepository.save(product);
		}

		Coupon coupon = null;
		BigDecimal discount = BigDecimal.ZERO;
		if (request.couponCode() != null && !request.couponCode().isBlank()) {
			coupon = couponRepository.findByCodeIgnoreCase(request.couponCode().trim())
					.filter(Coupon::isActive)
					.filter(c -> c.getExpiresAt() == null || c.getExpiresAt().isAfter(Instant.now()))
					.orElseThrow(() -> new ApiException("Invalid coupon", HttpStatus.BAD_REQUEST));
			discount = subtotal.multiply(coupon.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		}

		order.setSubtotal(subtotal);
		order.setDiscount(discount);
		order.setTotal(subtotal.subtract(discount));
		order.setCoupon(coupon);
		orderRepository.save(order);
		cartService.clearCart(user);
		mailNotificationService.sendOrderConfirmation(user.getEmail(), order.getId(), order.getTotal());
		return toOrder(order);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> myOrders(User user) {
		return orderRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toOrder).toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrderForCustomer(User user, Long orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
		if (!order.getCustomer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
			throw new ApiException("Order not found", HttpStatus.NOT_FOUND);
		}
		return toOrder(order);
	}

	@Transactional
	public OrderResponse cancelOrder(User user, Long orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
		if (!order.getCustomer().getId().equals(user.getId())) {
			throw new ApiException("Order not found", HttpStatus.NOT_FOUND);
		}
		if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PAID) {
			throw new ApiException("Order cannot be cancelled", HttpStatus.BAD_REQUEST);
		}
		order.setStatus(OrderStatus.CANCELLED);
		for (OrderItem item : order.getItems()) {
			Product product = item.getProduct();
			product.setStock(product.getStock() + item.getQuantity());
			productRepository.save(product);
		}
		return toOrder(order);
	}

	@Transactional
	public OrderResponse updateStatusForSeller(User seller, Long orderId, OrderStatus status) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
		boolean owns = order.getItems().stream().anyMatch(i -> i.getShop().getSeller().getId().equals(seller.getId()));
		if (!owns) {
			throw new ApiException("Order not found", HttpStatus.NOT_FOUND);
		}
		if (status != OrderStatus.SHIPPED && status != OrderStatus.DELIVERED) {
			throw new ApiException("Invalid status transition", HttpStatus.BAD_REQUEST);
		}
		order.setStatus(status);
		return toOrder(order);
	}

	public OrderEntity requireOrder(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
	}

	public OrderResponse toOrder(OrderEntity order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(i -> new OrderItemResponse(
						i.getProduct().getId(), i.getProduct().getName(), i.getShop().getId(), i.getShop().getName(),
						i.getQuantity(), i.getUnitPrice(), i.getLineTotal()))
				.toList();
		String couponCode = order.getCoupon() == null ? null : order.getCoupon().getCode();
		return new OrderResponse(order.getId(), order.getStatus(), order.getSubtotal(), order.getDiscount(), order.getTotal(),
				couponCode, order.getCreatedAt(), items);
	}

	private AddressResponse toAddress(Address address) {
		return new AddressResponse(address.getId(), address.getLine1(), address.getLine2(), address.getCity(),
				address.getState(), address.getPostalCode(), address.getCountry(), address.isDefaultAddress());
	}
}
