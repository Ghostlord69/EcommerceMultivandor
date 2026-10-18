package com.ecommerce.multivendor.repository;

import com.ecommerce.multivendor.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {
	Optional<Shop> findBySellerId(Long sellerId);
	List<Shop> findByApprovedFalse();
	List<Shop> findByApprovedTrue();
}
