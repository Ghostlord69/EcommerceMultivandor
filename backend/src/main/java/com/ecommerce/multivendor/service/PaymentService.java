package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.config.PaymentProperties;
import com.ecommerce.multivendor.domain.*;
import com.ecommerce.multivendor.dto.payment.*;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Creates sandbox payment intents for Razorpay and Stripe.
 * When API keys are placeholders, falls back to local mock references so demos work offline.
 */
@Service
public class PaymentService {
	private final PaymentRepository paymentRepository;
	private final OrderService orderService;
	private final PaymentProperties paymentProperties;

	public PaymentService(PaymentRepository paymentRepository, OrderService orderService, PaymentProperties paymentProperties) {
		this.paymentRepository = paymentRepository;
		this.orderService = orderService;
		this.paymentProperties = paymentProperties;
	}

	@Transactional
	public PaymentResponse createPayment(User user, CreatePaymentRequest request) {
		OrderEntity order = orderService.requireOrder(request.orderId());
		if (!order.getCustomer().getId().equals(user.getId())) {
			throw new ApiException("Order not found", HttpStatus.NOT_FOUND);
		}
		if (order.getStatus() != OrderStatus.PENDING) {
			throw new ApiException("Order is not payable", HttpStatus.BAD_REQUEST);
		}
		paymentRepository.findByOrderId(order.getId()).ifPresent(p -> {
			throw new ApiException("Payment already created", HttpStatus.CONFLICT);
		});

		String providerRef;
		String clientRef;
		String publishable = null;
		if (request.provider() == PaymentProvider.STRIPE) {
			StripeResult stripe = createStripe(order.getTotal());
			providerRef = stripe.intentId();
			clientRef = stripe.clientSecret();
			publishable = paymentProperties.stripe().apiKey().startsWith("sk_") ? "pk_test_demo" : "pk_test_demo";
		} else if (request.provider() == PaymentProvider.RAZORPAY) {
			RazorpayResult rz = createRazorpay(order.getTotal());
			providerRef = rz.orderId();
			clientRef = rz.orderId();
			publishable = paymentProperties.razorpay().keyId();
		} else {
			providerRef = "COD-" + UUID.randomUUID();
			clientRef = providerRef;
		}

		Payment payment = paymentRepository.save(Payment.builder()
				.order(order)
				.provider(request.provider())
				.status(request.provider() == PaymentProvider.COD ? PaymentStatus.CAPTURED : PaymentStatus.CREATED)
				.amount(order.getTotal())
				.providerReference(providerRef)
				.build());

		if (request.provider() == PaymentProvider.COD) {
			order.setStatus(OrderStatus.PAID);
		}

		return new PaymentResponse(payment.getId(), order.getId(), payment.getProvider(), payment.getStatus(),
				payment.getAmount(), payment.getProviderReference(), clientRef, publishable);
	}

	@Transactional
	public PaymentResponse confirmPayment(User user, ConfirmPaymentRequest request) {
		Payment payment = paymentRepository.findById(request.paymentId())
				.orElseThrow(() -> new ApiException("Payment not found", HttpStatus.NOT_FOUND));
		if (!payment.getOrder().getCustomer().getId().equals(user.getId())) {
			throw new ApiException("Payment not found", HttpStatus.NOT_FOUND);
		}
		payment.setProviderReference(request.providerReference());
		payment.setStatus(PaymentStatus.CAPTURED);
		payment.getOrder().setStatus(OrderStatus.PAID);
		return new PaymentResponse(payment.getId(), payment.getOrder().getId(), payment.getProvider(), payment.getStatus(),
				payment.getAmount(), payment.getProviderReference(), payment.getProviderReference(), null);
	}

	private StripeResult createStripe(BigDecimal amount) {
		String apiKey = paymentProperties.stripe().apiKey();
		if (!StringUtils.hasText(apiKey) || apiKey.contains("placeholder") || apiKey.contains("change-me")) {
			String id = "pi_mock_" + UUID.randomUUID();
			return new StripeResult(id, id + "_secret_mock");
		}
		try {
			Stripe.apiKey = apiKey;
			long cents = amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
			PaymentIntent intent = PaymentIntent.create(PaymentIntentCreateParams.builder()
					.setAmount(cents)
					.setCurrency("inr")
					.setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
					.build());
			return new StripeResult(intent.getId(), intent.getClientSecret());
		} catch (Exception ex) {
			throw new ApiException("Stripe error: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
		}
	}

	private RazorpayResult createRazorpay(BigDecimal amount) {
		String keyId = paymentProperties.razorpay().keyId();
		String keySecret = paymentProperties.razorpay().keySecret();
		if (!StringUtils.hasText(keyId) || keyId.contains("placeholder") || keyId.contains("change-me")) {
			return new RazorpayResult("order_mock_" + UUID.randomUUID());
		}
		try {
			RazorpayClient client = new RazorpayClient(keyId, keySecret);
			JSONObject options = new JSONObject();
			options.put("amount", amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact());
			options.put("currency", "INR");
			options.put("receipt", "rcpt_" + UUID.randomUUID());
			com.razorpay.Order order = client.orders.create(options);
			return new RazorpayResult(order.get("id"));
		} catch (RazorpayException ex) {
			throw new ApiException("Razorpay error: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
		}
	}

	private record StripeResult(String intentId, String clientSecret) {}
	private record RazorpayResult(String orderId) {}
}
