package com.ecommerce.multivendor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(
		Razorpay razorpay,
		Stripe stripe
) {
	public record Razorpay(String keyId, String keySecret) {}
	public record Stripe(String apiKey, String webhookSecret) {}
}
