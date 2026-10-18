package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.chatbot.ChatIntent;
import com.ecommerce.multivendor.chatbot.IntentMatcher;
import com.ecommerce.multivendor.domain.Role;
import com.ecommerce.multivendor.domain.User;
import com.ecommerce.multivendor.dto.cart.CartResponse;
import com.ecommerce.multivendor.dto.chat.ChatResponse;
import com.ecommerce.multivendor.dto.order.OrderResponse;
import com.ecommerce.multivendor.dto.product.CategoryResponse;
import com.ecommerce.multivendor.dto.product.ProductResponse;
import com.ecommerce.multivendor.dto.seller.SellerDashboardResponse;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatbotService {
	private final IntentMatcher intentMatcher;
	private final OrderService orderService;
	private final CartService cartService;
	private final CatalogService catalogService;
	private final WishlistService wishlistService;
	private final SellerService sellerService;
	private final AdminService adminService;
	private final CategoryRepository categoryRepository;

	public ChatbotService(IntentMatcher intentMatcher, OrderService orderService, CartService cartService,
			CatalogService catalogService, WishlistService wishlistService, SellerService sellerService,
			AdminService adminService, CategoryRepository categoryRepository) {
		this.intentMatcher = intentMatcher;
		this.orderService = orderService;
		this.cartService = cartService;
		this.catalogService = catalogService;
		this.wishlistService = wishlistService;
		this.sellerService = sellerService;
		this.adminService = adminService;
		this.categoryRepository = categoryRepository;
	}

	@Transactional(readOnly = true)
	public ChatResponse chat(User user, String message) {
		IntentMatcher.MatchResult match = intentMatcher.match(message);
		return switch (match.intent()) {
			case GREETING -> reply(match.intent(), "Hi " + user.getFullName() + "! Ask me about orders, cart, products, wishlist, or type help.");
			case HELP -> reply(match.intent(), """
					I understand: my orders | order #123 | cart | wishlist | product <name> | category <name> | \
					returns | payments | shipping. Sellers: pending orders, low stock. Admins: pending approvals.""");
			case FAQ_RETURNS -> reply(match.intent(), "Returns are accepted within 7 days of delivery for unused items. Start a return from Order details.");
			case FAQ_PAYMENTS -> reply(match.intent(), "We support Razorpay, Stripe (test/sandbox), and Cash on Delivery. Cards and UPI work in checkout.");
			case FAQ_SHIPPING -> reply(match.intent(), "Standard shipping takes 3–5 business days after payment. Track status with: order #ID status.");
			case LIST_ORDERS -> listOrders(user, match.intent());
			case ORDER_DETAIL, ORDER_STATUS -> orderDetail(user, match);
			case CANCEL_ORDER -> cancel(user, match);
			case SHOW_CART -> showCart(user, match.intent());
			case PRODUCT_SEARCH -> searchProducts(match);
			case PRODUCT_BY_CATEGORY -> byCategory(match);
			case SHOW_WISHLIST -> showWishlist(user, match.intent());
			case SELLER_PENDING_ORDERS, SELLER_LOW_STOCK -> sellerSummary(user, match.intent());
			case ADMIN_PENDING_APPROVALS -> adminSummary(user, match.intent());
			case UNKNOWN -> reply(ChatIntent.UNKNOWN, "Sorry, I didn't understand. Type help for supported commands.");
		};
	}

	private ChatResponse listOrders(User user, ChatIntent intent) {
		List<OrderResponse> orders = orderService.myOrders(user);
		if (orders.isEmpty()) {
			return reply(intent, "You have no orders yet.");
		}
		String body = orders.stream().limit(5)
				.map(o -> "#" + o.id() + " " + o.status() + " total " + o.total())
				.collect(Collectors.joining("; "));
		return reply(intent, "Recent orders: " + body);
	}

	private ChatResponse orderDetail(User user, IntentMatcher.MatchResult match) {
		Long id = match.orderId().orElseThrow(() -> new ApiException("Please include an order id, e.g. order #12", HttpStatus.BAD_REQUEST));
		OrderResponse order = orderService.getOrderForCustomer(user, id);
		return reply(match.intent(), "Order #" + order.id() + " is " + order.status() + ". Total " + order.total()
				+ ". Items: " + order.items().size());
	}

	private ChatResponse cancel(User user, IntentMatcher.MatchResult match) {
		Long id = match.orderId().orElseThrow(() -> new ApiException("Please include an order id to cancel", HttpStatus.BAD_REQUEST));
		OrderResponse order = orderService.cancelOrder(user, id);
		return reply(match.intent(), "Order #" + order.id() + " cancelled. Status is now " + order.status() + ".");
	}

	private ChatResponse showCart(User user, ChatIntent intent) {
		CartResponse cart = cartService.getCart(user);
		if (cart.itemCount() == 0) {
			return reply(intent, "Your cart is empty.");
		}
		String items = cart.items().stream()
				.map(i -> i.productName() + " x" + i.quantity())
				.collect(Collectors.joining(", "));
		return reply(intent, "Cart (" + cart.itemCount() + " items): " + items + ". Total " + cart.total());
	}

	private ChatResponse searchProducts(IntentMatcher.MatchResult match) {
		String q = match.query().orElse("");
		List<ProductResponse> products = catalogService.listProducts(q, null);
		if (products.isEmpty()) {
			return reply(match.intent(), "No products matched '" + q + "'.");
		}
		String body = products.stream().limit(5)
				.map(p -> p.name() + " (₹" + p.price() + ")")
				.collect(Collectors.joining("; "));
		return reply(match.intent(), "Found: " + body);
	}

	private ChatResponse byCategory(IntentMatcher.MatchResult match) {
		String name = match.query().orElse("");
		Long categoryId = categoryRepository.findByNameIgnoreCase(name).map(c -> c.getId()).orElse(null);
		if (categoryId == null) {
			List<CategoryResponse> cats = catalogService.listCategories();
			String available = cats.stream().map(CategoryResponse::name).collect(Collectors.joining(", "));
			return reply(match.intent(), "Unknown category. Try: " + available);
		}
		List<ProductResponse> products = catalogService.listProducts(null, categoryId);
		String body = products.stream().limit(5).map(ProductResponse::name).collect(Collectors.joining(", "));
		return reply(match.intent(), products.isEmpty() ? "No products in that category." : "In " + name + ": " + body);
	}

	private ChatResponse showWishlist(User user, ChatIntent intent) {
		List<ProductResponse> items = wishlistService.list(user);
		if (items.isEmpty()) {
			return reply(intent, "Wishlist is empty.");
		}
		return reply(intent, "Wishlist: " + items.stream().map(ProductResponse::name).collect(Collectors.joining(", ")));
	}

	private ChatResponse sellerSummary(User user, ChatIntent intent) {
		if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
			return reply(intent, "Seller insights are only available to seller accounts.");
		}
		SellerDashboardResponse dash = sellerService.dashboard(user);
		if (intent == ChatIntent.SELLER_LOW_STOCK) {
			return reply(intent, "Low-stock products: " + dash.lowStockProducts());
		}
		return reply(intent, "Pending seller orders: " + dash.pendingOrders() + ". Earnings: " + dash.earnings());
	}

	private ChatResponse adminSummary(User user, ChatIntent intent) {
		if (user.getRole() != Role.ADMIN) {
			return reply(intent, "Pending approvals are only available to admins.");
		}
		return reply(intent, "Pending seller approvals: " + adminService.dashboard().pendingSellerApprovals());
	}

	private ChatResponse reply(ChatIntent intent, String text) {
		return new ChatResponse(intent.name(), text);
	}
}
