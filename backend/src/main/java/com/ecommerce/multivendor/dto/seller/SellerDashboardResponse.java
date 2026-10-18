package com.ecommerce.multivendor.dto.seller;

import java.math.BigDecimal;
import java.util.List;

public record SellerDashboardResponse(
		long pendingOrders, long lowStockProducts, BigDecimal earnings, List<NamedCount> salesByDay
) {
	public record NamedCount(String label, long count) {}
}
