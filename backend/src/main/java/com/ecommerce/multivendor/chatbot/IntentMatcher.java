package com.ecommerce.multivendor.chatbot;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Keyword / regex based intent classifier — no LLM. */
@Component
public class IntentMatcher {
	private static final Pattern ORDER_ID = Pattern.compile("(?:order\\s*#?|#)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern CATEGORY = Pattern.compile("(?:category|in)\\s+([a-zA-Z][a-zA-Z0-9 \\-]{1,40})", Pattern.CASE_INSENSITIVE);
	private static final Pattern PRODUCT = Pattern.compile("(?:product|find|search|show)\\s+(.+)", Pattern.CASE_INSENSITIVE);

	public record MatchResult(ChatIntent intent, Optional<Long> orderId, Optional<String> query) {}

	public MatchResult match(String raw) {
		String message = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
		if (message.isEmpty()) {
			return new MatchResult(ChatIntent.UNKNOWN, Optional.empty(), Optional.empty());
		}

		Matcher orderMatcher = ORDER_ID.matcher(raw);
		Optional<Long> orderId = orderMatcher.find() ? Optional.of(Long.valueOf(orderMatcher.group(1))) : Optional.empty();

		if (containsAny(message, "hello", "hi", "hey", "good morning", "good evening")) {
			return new MatchResult(ChatIntent.GREETING, orderId, Optional.empty());
		}
		if (containsAny(message, "help", "commands", "what can you", "menu")) {
			return new MatchResult(ChatIntent.HELP, orderId, Optional.empty());
		}
		if (containsAny(message, "return", "refund policy", "exchange")) {
			return new MatchResult(ChatIntent.FAQ_RETURNS, orderId, Optional.empty());
		}
		if (containsAny(message, "payment", "razorpay", "stripe", "pay how", "cod")) {
			return new MatchResult(ChatIntent.FAQ_PAYMENTS, orderId, Optional.empty());
		}
		if (containsAny(message, "shipping", "delivery", "arrive", "ship")) {
			return new MatchResult(ChatIntent.FAQ_SHIPPING, orderId, Optional.empty());
		}
		if (containsAny(message, "cancel order") || (message.contains("cancel") && orderId.isPresent())) {
			return new MatchResult(ChatIntent.CANCEL_ORDER, orderId, Optional.empty());
		}
		if (orderId.isPresent() && containsAny(message, "status", "track", "where")) {
			return new MatchResult(ChatIntent.ORDER_STATUS, orderId, Optional.empty());
		}
		if (orderId.isPresent() || containsAny(message, "order detail", "show order")) {
			if (orderId.isPresent()) {
				return new MatchResult(ChatIntent.ORDER_DETAIL, orderId, Optional.empty());
			}
		}
		if (containsAny(message, "my orders", "order history", "list orders", "orders")) {
			return new MatchResult(ChatIntent.LIST_ORDERS, orderId, Optional.empty());
		}
		if (containsAny(message, "cart", "basket", "cart total")) {
			return new MatchResult(ChatIntent.SHOW_CART, orderId, Optional.empty());
		}
		if (containsAny(message, "wishlist", "wish list", "saved items")) {
			return new MatchResult(ChatIntent.SHOW_WISHLIST, orderId, Optional.empty());
		}
		if (containsAny(message, "pending approval", "seller approval")) {
			return new MatchResult(ChatIntent.ADMIN_PENDING_APPROVALS, orderId, Optional.empty());
		}
		if (containsAny(message, "low stock", "out of stock")) {
			return new MatchResult(ChatIntent.SELLER_LOW_STOCK, orderId, Optional.empty());
		}
		if (containsAny(message, "pending order", "seller orders")) {
			return new MatchResult(ChatIntent.SELLER_PENDING_ORDERS, orderId, Optional.empty());
		}
		Matcher cat = CATEGORY.matcher(raw);
		if (cat.find()) {
			return new MatchResult(ChatIntent.PRODUCT_BY_CATEGORY, orderId, Optional.of(cat.group(1).trim()));
		}
		Matcher prod = PRODUCT.matcher(raw);
		if (prod.find()) {
			return new MatchResult(ChatIntent.PRODUCT_SEARCH, orderId, Optional.of(prod.group(1).trim()));
		}
		if (orderId.isPresent()) {
			return new MatchResult(ChatIntent.ORDER_DETAIL, orderId, Optional.empty());
		}
		return new MatchResult(ChatIntent.UNKNOWN, orderId, Optional.empty());
	}

	private boolean containsAny(String message, String... needles) {
		for (String n : needles) {
			if (message.contains(n)) {
				return true;
			}
		}
		return false;
	}
}
