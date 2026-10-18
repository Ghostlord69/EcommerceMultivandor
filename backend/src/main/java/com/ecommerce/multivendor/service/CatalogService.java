package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.domain.Category;
import com.ecommerce.multivendor.domain.Product;
import com.ecommerce.multivendor.dto.product.*;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.CategoryRepository;
import com.ecommerce.multivendor.repository.ProductRepository;
import com.ecommerce.multivendor.repository.ReviewRepository;
import com.ecommerce.multivendor.domain.Review;
import com.ecommerce.multivendor.domain.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ReviewRepository reviewRepository;

	public CatalogService(ProductRepository productRepository, CategoryRepository categoryRepository, ReviewRepository reviewRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.reviewRepository = reviewRepository;
	}

	public List<ProductResponse> listProducts(String q, Long categoryId) {
		List<Product> products;
		if (q != null && !q.isBlank()) {
			products = productRepository.findByActiveTrueAndNameContainingIgnoreCase(q.trim());
		} else if (categoryId != null) {
			products = productRepository.findByActiveTrueAndCategoryId(categoryId);
		} else {
			products = productRepository.findByActiveTrue();
		}
		return products.stream().map(ProductMapper::toResponse).toList();
	}

	public ProductResponse getProduct(Long id) {
		return ProductMapper.toResponse(requireActiveProduct(id));
	}

	public Product requireActiveProduct(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));
		if (!product.isActive()) {
			throw new ApiException("Product not available", HttpStatus.NOT_FOUND);
		}
		return product;
	}

	public List<CategoryResponse> listCategories() {
		return categoryRepository.findAll().stream()
				.map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription()))
				.toList();
	}

	@Transactional
	public CategoryResponse createCategory(CategoryRequest request) {
		categoryRepository.findByNameIgnoreCase(request.name()).ifPresent(c -> {
			throw new ApiException("Category already exists", HttpStatus.CONFLICT);
		});
		Category category = categoryRepository.save(Category.builder().name(request.name()).description(request.description()).build());
		return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
	}

	@Transactional
	public ReviewResponse addReview(User customer, Long productId, ReviewRequest request) {
		Product product = requireActiveProduct(productId);
		reviewRepository.findByProductIdAndCustomerId(productId, customer.getId()).ifPresent(r -> {
			throw new ApiException("Already reviewed", HttpStatus.CONFLICT);
		});
		Review review = reviewRepository.save(Review.builder()
				.product(product)
				.customer(customer)
				.rating(request.rating())
				.comment(request.comment())
				.build());
		return new ReviewResponse(review.getId(), productId, customer.getFullName(), review.getRating(), review.getComment(), review.getCreatedAt());
	}

	public List<ReviewResponse> listReviews(Long productId) {
		return reviewRepository.findByProductId(productId).stream()
				.map(r -> new ReviewResponse(r.getId(), productId, r.getCustomer().getFullName(), r.getRating(), r.getComment(), r.getCreatedAt()))
				.toList();
	}
}
