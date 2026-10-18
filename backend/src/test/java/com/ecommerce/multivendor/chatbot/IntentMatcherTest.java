package com.ecommerce.multivendor.chatbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IntentMatcherTest {

	private final IntentMatcher matcher = new IntentMatcher();

	@Test
	void detectsCartIntent() {
		assertEquals(ChatIntent.SHOW_CART, matcher.match("show my cart please").intent());
	}

	@Test
	void extractsOrderId() {
		var result = matcher.match("what is the status of order #42");
		assertEquals(ChatIntent.ORDER_STATUS, result.intent());
		assertTrue(result.orderId().isPresent());
		assertEquals(42L, result.orderId().orElseThrow());
	}

	@Test
	void detectsHelp() {
		assertEquals(ChatIntent.HELP, matcher.match("help").intent());
	}
}
