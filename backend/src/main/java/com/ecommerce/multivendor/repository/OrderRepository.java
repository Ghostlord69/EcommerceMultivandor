package com.ecommerce.multivendor.repository;

import com.ecommerce.multivendor.domain.OrderEntity;
import com.ecommerce.multivendor.domain.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
	List<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

	@Query("select distinct o from OrderEntity o join o.items i where i.shop.seller.id = :sellerId order by o.createdAt desc")
	List<OrderEntity> findByItemsShopSellerIdOrderByCreatedAtDesc(@Param("sellerId") Long sellerId);

	long countByStatus(OrderStatus status);
}
