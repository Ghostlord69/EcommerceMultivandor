package com.ecommerce.multivendor.controller;

import com.ecommerce.multivendor.dto.payment.*;
import com.ecommerce.multivendor.security.AuthenticatedUser;
import com.ecommerce.multivendor.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping
	public PaymentResponse create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CreatePaymentRequest request) {
		return paymentService.createPayment(user.getUser(), request);
	}

	@PostMapping("/confirm")
	public PaymentResponse confirm(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ConfirmPaymentRequest request) {
		return paymentService.confirmPayment(user.getUser(), request);
	}
}
