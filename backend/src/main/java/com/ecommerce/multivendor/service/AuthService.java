package com.ecommerce.multivendor.service;

import com.ecommerce.multivendor.domain.Role;
import com.ecommerce.multivendor.domain.User;
import com.ecommerce.multivendor.dto.auth.AuthResponse;
import com.ecommerce.multivendor.dto.auth.LoginRequest;
import com.ecommerce.multivendor.dto.auth.RegisterRequest;
import com.ecommerce.multivendor.exception.ApiException;
import com.ecommerce.multivendor.repository.UserRepository;
import com.ecommerce.multivendor.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (request.role() == Role.ADMIN) {
			throw new ApiException("Cannot self-register as ADMIN", HttpStatus.BAD_REQUEST);
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new ApiException("Email already registered", HttpStatus.CONFLICT);
		}
		User user = User.builder()
				.email(request.email().toLowerCase())
				.passwordHash(passwordEncoder.encode(request.password()))
				.fullName(request.fullName())
				.role(request.role())
				.build();
		userRepository.save(user);
		return toAuth(user);
	}

	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
		User user = userRepository.findByEmail(request.email().toLowerCase())
				.orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
		return toAuth(user);
	}

	private AuthResponse toAuth(User user) {
		return new AuthResponse(jwtService.generateToken(user), user.getId(), user.getEmail(), user.getFullName(), user.getRole());
	}
}
