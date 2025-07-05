package com.java.fashionshop.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaUser;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final JpaUser userRepository;

    public CustomUserDetailsService(JpaUser userRepository) {
        this.userRepository = userRepository;
    }
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // Ensure password is encoded
                .roles(user.getRole() == 0 ? "ADMIN" : "USER") // Use the enum name as the role
                .build();
	}

}
