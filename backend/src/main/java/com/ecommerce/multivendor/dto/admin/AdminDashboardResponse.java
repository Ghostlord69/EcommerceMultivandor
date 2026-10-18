package com.ecommerce.multivendor.dto.admin;

import java.util.List;

public record AdminDashboardResponse(
		long pendingSellerApprovals, long totalOrders, long activeCoupons, List<NamedCount> ordersByStatus
) {
	public record NamedCount(String label, long count) {}
}
