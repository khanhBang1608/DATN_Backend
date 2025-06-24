package com.java.fashionshop.component;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
          final String authorizationHeader = request.getHeader("Authorization");

    String email = null;
    String jwt = null;

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        jwt = authorizationHeader.substring(7);
        email = jwtUtil.extractUsername(jwt);
    }

    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        String role = jwtUtil.extractRole(jwt);

        // Create authentication token
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(role)));

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
  
          filterChain.doFilter(request, response);
    }
	
}

