package com.ecommerce.multivendor.config;

import com.ecommerce.multivendor.domain.*;
import com.ecommerce.multivendor.repository.*;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds demo admin/seller/customer accounts, categories, shop, and sample products. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final ShopRepository shopRepository;
	private final ProductRepository productRepository;
	private final CouponRepository couponRepository;
	private final PasswordEncoder passwordEncoder;

	public DataSeeder(UserRepository userRepository, CategoryRepository categoryRepository, ShopRepository shopRepository,
			ProductRepository productRepository, CouponRepository couponRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.categoryRepository = categoryRepository;
		this.shopRepository = shopRepository;
		this.productRepository = productRepository;
		this.couponRepository = couponRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (userRepository.count() > 0) {
			return;
		}
		User admin = userRepository.save(User.builder()
				.email("admin@shop.com")
				.passwordHash(passwordEncoder.encode("password123"))
				.fullName("Platform Admin")
				.role(Role.ADMIN)
				.build());
		User seller = userRepository.save(User.builder()
				.email("seller@shop.com")
				.passwordHash(passwordEncoder.encode("password123"))
				.fullName("Demo Seller")
				.role(Role.SELLER)
				.build());
		userRepository.save(User.builder()
				.email("customer@shop.com")
				.passwordHash(passwordEncoder.encode("password123"))
				.fullName("Demo Customer")
				.role(Role.CUSTOMER)
				.build());

		Category electronics = categoryRepository.save(Category.builder().name("Electronics").description("Gadgets and devices").build());
		Category fashion = categoryRepository.save(Category.builder().name("Fashion").description("Apparel").build());
		Category home = categoryRepository.save(Category.builder().name("Home").description("Home & living").build());

		Shop shop = shopRepository.save(Shop.builder()
				.seller(seller)
				.name("Demo Gadgets")
				.description("Approved demo seller shop")
				.approved(true)
				.build());

		productRepository.save(Product.builder().shop(shop).category(electronics).name("Wireless Headphones")
				.description("Noise-cancelling over-ear headphones").price(new BigDecimal("2999.00")).stock(40)
				.imageUrl("https://picsum.photos/seed/headphones/600/400").active(true).build());
		productRepository.save(Product.builder().shop(shop).category(electronics).name("USB-C Hub")
				.description("7-in-1 multiport adapter").price(new BigDecimal("1499.00")).stock(80)
				.imageUrl("https://picsum.photos/seed/hub/600/400").active(true).build());
		productRepository.save(Product.builder().shop(shop).category(fashion).name("Canvas Sneakers")
				.description("Everyday unisex sneakers").price(new BigDecimal("2199.00")).stock(55)
				.imageUrl("https://picsum.photos/seed/sneakers/600/400").active(true).build());
		productRepository.save(Product.builder().shop(shop).category(home).name("Ceramic Mug Set")
				.description("Set of 4 matte mugs").price(new BigDecimal("799.00")).stock(120)
				.imageUrl("https://picsum.photos/seed/mugs/600/400").active(true).build());

		couponRepository.save(Coupon.builder().code("WELCOME10").discountPercent(new BigDecimal("10.00")).active(true).build());

		log.info("Seeded demo users (admin/seller/customer@shop.com / password123), shop, products. Admin id={}", admin.getId());
	}
}
