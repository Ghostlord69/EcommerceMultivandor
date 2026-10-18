package com.ecommerce.multivendor.repository;

import com.ecommerce.multivendor.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByProductId(Long productId);
	Optional<Review> findByProductIdAndCustomerId(Long productId, Long customerId);
}
