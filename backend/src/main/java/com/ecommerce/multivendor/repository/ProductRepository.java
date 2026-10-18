package com.ecommerce.multivendor.repository;

import com.ecommerce.multivendor.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByActiveTrue();
	List<Product> findByActiveTrueAndCategoryId(Long categoryId);
	List<Product> findByActiveTrueAndNameContainingIgnoreCase(String name);
	List<Product> findByShopId(Long shopId);
	List<Product> findByShopSellerIdAndStockLessThanEqual(Long sellerId, int stock);
}
