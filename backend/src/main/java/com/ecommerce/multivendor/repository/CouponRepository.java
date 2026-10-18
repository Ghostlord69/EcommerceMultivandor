package com.ecommerce.multivendor.repository;

import com.ecommerce.multivendor.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
	Optional<Coupon> findByCodeIgnoreCase(String code);
	List<Coupon> findByActiveTrue();
}
