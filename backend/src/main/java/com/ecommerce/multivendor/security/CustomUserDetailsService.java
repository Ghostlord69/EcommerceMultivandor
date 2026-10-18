package com.ecommerce.multivendor.security;

import com.ecommerce.multivendor.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByEmail(username)
				.map(AuthenticatedUser::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

	public AuthenticatedUser loadById(Long id) {
		return userRepository.findById(id)
				.map(AuthenticatedUser::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
}
