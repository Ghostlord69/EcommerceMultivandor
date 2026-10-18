package com.ecommerce.multivendor.security;

import com.ecommerce.multivendor.config.JwtProperties;
import com.ecommerce.multivendor.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Issues and validates HS256 JWT access tokens. */
@Service
public class JwtService {
	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(User user) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.expirationMs());
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("email", user.getEmail())
				.claim("role", user.getRole().name())
				.issuedAt(now)
				.expiration(expiry)
				.signWith(secretKey)
				.compact();
	}

	public Long extractUserId(String token) {
		return Long.valueOf(parseClaims(token).getSubject());
	}

	public boolean isTokenValid(String token) {
		try {
			return parseClaims(token).getExpiration().after(new Date());
		} catch (Exception ex) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
	}
}
