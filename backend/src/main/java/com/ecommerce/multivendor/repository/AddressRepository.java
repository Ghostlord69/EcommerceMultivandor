package com.ecommerce.multivendor.repository;

import com.ecommerce.multivendor.domain.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
	List<Address> findByUserId(Long userId);
}
