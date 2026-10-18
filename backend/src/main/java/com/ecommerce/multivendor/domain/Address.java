package com.ecommerce.multivendor.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, length = 120)
	private String line1;

	@Column(length = 120)
	private String line2;

	@Column(nullable = false, length = 80)
	private String city;

	@Column(nullable = false, length = 80)
	private String state;

	@Column(nullable = false, length = 20)
	private String postalCode;

	@Column(nullable = false, length = 80)
	private String country;

	@Column(nullable = false)
	private boolean defaultAddress;
}
