package com.java.fashionshop.contrtoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.services.UserService;
import com.java.fashionshop.dto.LoginResponseDTO;
import com.java.fashionshop.dto.UserDTO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/login")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class LoginController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping
	public ResponseEntity<?> login(@RequestParam("email") String email, @RequestParam("password") String password,
			HttpServletResponse response) {

		return userService.findByEmail(email).map(user -> {
			if (!user.getStatus()) {
				return ResponseEntity.badRequest().body("Tài khoản đã bị khóa");
			}

			if (passwordEncoder.matches(password, user.getPassword())) {
				Authentication authentication = authenticationManager
						.authenticate(new UsernamePasswordAuthenticationToken(email, password));

				String role = authentication.getAuthorities().iterator().next().getAuthority();
				String token = jwtUtil.generateToken(email, role);

				UserDTO userDTO = new UserDTO(user.getUserId(), user.getFullName(), user.getEmail(), user.getAvatar(),
						user.getStatus(), user.getRole());

				Cookie cookie = new Cookie("token", token);
				cookie.setHttpOnly(true);
				cookie.setPath("/");
				cookie.setMaxAge(2 * 60 * 60);
				response.addCookie(cookie);

				return ResponseEntity.ok(new LoginResponseDTO(token, userDTO));
			} else {
				return ResponseEntity.badRequest().body("Đăng nhập không thành công.!");
			}
		}).orElse(ResponseEntity.badRequest().body("Đăng nhập không thành công.!"));
	}
}
