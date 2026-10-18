package com.ecommerce.multivendor.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "shop_id")
	private Shop shop;

	@ManyToOne(optional = false)
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(nullable = false)
	private int stock;

	@Column(length = 500)
	private String imageUrl;

	@Column(nullable = false)
	private boolean active;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
