package com.ecommerce.multivendor.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 40)
	private String code;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal discountPercent;

	@Column(nullable = false)
	private boolean active;

	private Instant expiresAt;
}
