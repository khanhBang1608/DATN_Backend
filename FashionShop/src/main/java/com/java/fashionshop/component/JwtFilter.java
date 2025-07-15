package com.java.fashionshop.component;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
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

        String requestPath = request.getServletPath();

        // ✅ Bỏ qua filter với các API công khai
        if (requestPath.startsWith("/api/public")
            || requestPath.startsWith("/api/login")
            || requestPath.startsWith("/api/register")
            || requestPath.startsWith("/images")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);

            try {
                email = jwtUtil.extractEmail(jwt); // ❗ Có thể ném ExpiredJwtException
            } catch (ExpiredJwtException e) {
                System.out.println("⚠️ Token hết hạn: " + e.getMessage());
                // Bạn có thể return 401 tại đây nếu muốn
                // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
                // return;

                // Hoặc bỏ qua filter và cho tiếp tục chạy như anonymous user
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                System.out.println("⚠️ Token không hợp lệ: " + e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = jwtUtil.extractRole(jwt);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(role)));

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }
}
