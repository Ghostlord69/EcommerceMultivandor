package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.domain.*;
import com.ecommerce.multivendor.dto.admin.*;
import com.ecommerce.multivendor.dto.common.MessageResponse;
import com.ecommerce.multivendor.dto.seller.ShopResponse;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.*;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
	private final ShopRepository shopRepository;
	private final CouponRepository couponRepository;
	private final OrderRepository orderRepository;
	private final MailNotificationService mailNotificationService;

	public AdminService(ShopRepository shopRepository, CouponRepository couponRepository, OrderRepository orderRepository,
			MailNotificationService mailNotificationService) {
		this.shopRepository = shopRepository;
		this.couponRepository = couponRepository;
		this.orderRepository = orderRepository;
		this.mailNotificationService = mailNotificationService;
	}

	@Transactional(readOnly = true)
	public List<ShopResponse> pendingShops() {
		return shopRepository.findByApprovedFalse().stream().map(this::toShop).toList();
	}

	@Transactional
	public MessageResponse approveShop(Long shopId, boolean approved) {
		Shop shop = shopRepository.findById(shopId)
				.orElseThrow(() -> new ApiException("Shop not found", HttpStatus.NOT_FOUND));
		shop.setApproved(approved);
		mailNotificationService.sendSellerApproval(shop.getSeller().getEmail(), shop.getName(), approved);
		return new MessageResponse(approved ? "Shop approved" : "Shop rejected");
	}

	@Transactional
	public Coupon createCoupon(CouponRequest request) {
		couponRepository.findByCodeIgnoreCase(request.code()).ifPresent(c -> {
			throw new ApiException("Coupon exists", HttpStatus.CONFLICT);
		});
		return couponRepository.save(Coupon.builder()
				.code(request.code().toUpperCase())
				.discountPercent(request.discountPercent())
				.active(request.active())
				.expiresAt(request.expiresAt())
				.build());
	}

	@Transactional(readOnly = true)
	public List<Coupon> listCoupons() {
		return couponRepository.findAll();
	}

	@Transactional(readOnly = true)
	public AdminDashboardResponse dashboard() {
		long pending = shopRepository.findByApprovedFalse().size();
		long totalOrders = orderRepository.count();
		long activeCoupons = couponRepository.findByActiveTrue().size();
		List<AdminDashboardResponse.NamedCount> byStatus = Arrays.stream(OrderStatus.values())
				.map(s -> new AdminDashboardResponse.NamedCount(s.name(), orderRepository.countByStatus(s)))
				.toList();
		return new AdminDashboardResponse(pending, totalOrders, activeCoupons, byStatus);
	}

	private ShopResponse toShop(Shop shop) {
		return new ShopResponse(shop.getId(), shop.getName(), shop.getDescription(), shop.isApproved(),
				shop.getSeller().getId(), shop.getSeller().getFullName());
	}
}
