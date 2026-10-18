package com.ecommerce.multivendor.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "cart_id")
	private Cart cart;

	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	@Column(nullable = false)
	private int quantity;
}
