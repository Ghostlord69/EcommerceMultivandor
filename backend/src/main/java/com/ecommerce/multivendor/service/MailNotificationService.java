package com.ecommerce.multivendor.service;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends transactional emails; failures are logged so checkout is never blocked. */
@Service
public class MailNotificationService {
	private static final Logger log = LoggerFactory.getLogger(MailNotificationService.class);
	private final JavaMailSender mailSender;

	public MailNotificationService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendOrderConfirmation(String to, Long orderId, BigDecimal total) {
		send(to, "Order #" + orderId + " confirmed",
				"Thanks for shopping with EcommerceMultiVendor. Your order total is " + total + ".");
	}

	public void sendSellerApproval(String to, String shopName, boolean approved) {
		send(to, "Shop " + (approved ? "approved" : "rejected"),
				"Your shop '" + shopName + "' was " + (approved ? "approved" : "rejected") + " by admin.");
	}

	private void send(String to, String subject, String body) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
		} catch (Exception ex) {
			log.warn("Failed to send mail to {}: {}", to, ex.getMessage());
		}
	}
}
