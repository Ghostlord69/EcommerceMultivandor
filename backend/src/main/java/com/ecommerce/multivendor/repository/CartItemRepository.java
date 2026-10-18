package com.ecommerce.multivendor.repository;

import com.ecommerce.multivendor.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
	void deleteByCartId(Long cartId);
}
