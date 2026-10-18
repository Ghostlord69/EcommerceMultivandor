package com.ecommerce.multivendor.repository;

import com.ecommerce.multivendor.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
	List<WishlistItem> findByUserId(Long userId);
	Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);
	boolean existsByUserIdAndProductId(Long userId, Long productId);

	@Modifying
	@Transactional
	void deleteByUserIdAndProductId(Long userId, Long productId);
}
