package com.ecommerce.multivendor.controller;

import com.ecommerce.multivendor.dto.chat.ChatRequest;
import com.ecommerce.multivendor.dto.chat.ChatResponse;
import com.ecommerce.multivendor.security.AuthenticatedUser;
import com.ecommerce.multivendor.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
	private final ChatbotService chatbotService;

	public ChatController(ChatbotService chatbotService) {
		this.chatbotService = chatbotService;
	}

	@PostMapping
	public ChatResponse chat(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ChatRequest request) {
		return chatbotService.chat(user.getUser(), request.message());
	}
}
