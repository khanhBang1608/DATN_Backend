package com.java.fashionshop.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.java.fashionshop.bean.RegisterBean;
import com.java.fashionshop.entity.CartEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaUser;
import com.java.fashionshop.services.CartService;
import com.java.fashionshop.services.EmailService;
import com.java.fashionshop.services.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RestController
@RequestMapping("/api")
public class RegisterController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private JpaUser userJPA;

	@Autowired
	private EmailService emailService;

	@Autowired
	private CartService cartService;
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody RegisterBean registerBean, BindingResult result,
			HttpSession session) {
		Map<String, String> errors = new HashMap<>();

		// Lỗi validate từ annotation
		if (result.hasErrors()) {
			for (FieldError err : result.getFieldErrors()) {
				errors.put(err.getField(), err.getDefaultMessage());
			}
		}

		// Kiểm tra mật khẩu và xác nhận mật khẩu
		if (!registerBean.getPassword().equals(registerBean.getConfirmPassword())) {
			errors.put("confirmPassword", "Mật khẩu và xác nhận không khớp.");
		}

		// Kiểm tra email trùng
		if (userService.isEmailExists(registerBean.getEmail())) {
			errors.put("email", "Email đã được sử dụng.");
		}

		// Kiểm tra email đã xác thực OTP chưa
		String verifiedEmail = (String) session.getAttribute("verifiedEmail");
		if (verifiedEmail == null || !verifiedEmail.equals(registerBean.getEmail())) {
			errors.put("otp", "Email chưa được xác thực OTP.");
		}

		if (!errors.isEmpty()) {
			return ResponseEntity.badRequest().body(errors);
		}

		UserEntity savedUser = userService.registerUser(registerBean);

		// Tạo giỏ hàng cho user vừa đăng ký
		CartEntity cart = new CartEntity();
		cart.setUser(savedUser);
		cartService.save(cart);

		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/register/otp")
	public ResponseEntity<?> handleOtpFromVue(@RequestParam("email") String email,
			@RequestParam(value = "otp", required = false) String userOtp, HttpSession session) {

		Map<String, String> result = new HashMap<>();

		// Gửi OTP nếu chưa nhập OTP
		if (userOtp == null || userOtp.isEmpty()) {
			Optional<UserEntity> userOpt = userJPA.findByEmail(email);
			if (userOpt.isPresent()) {
				result.put("error", "Email đã được sử dụng. Vui lòng sử dụng email khác!");
				return ResponseEntity.badRequest().body(result);
			}

			String otp = generateOtp();
			session.setAttribute("otp", otp);
			session.setAttribute("otpEmail", email);
			session.setMaxInactiveInterval(180); // 3 phút

			try {
				emailService.sendEmail(email, "Mã OTP của bạn",
						"Mã OTP của bạn là: " + otp + ". Có hiệu lực trong 3 phút.");
				result.put("message", "OTP đã được gửi đến email của bạn.");
				return ResponseEntity.ok(result);
			} catch (Exception e) {
				result.put("error", "Gửi email thất bại. Vui lòng thử lại.");
				return ResponseEntity.status(500).body(result);
			}
		}

		// Người dùng đã nhập OTP → xác thực
		String storedOtp = (String) session.getAttribute("otp");
		String storedEmail = (String) session.getAttribute("otpEmail");

		if (storedOtp == null || storedEmail == null) {
			result.put("error", "OTP đã hết hạn hoặc không tồn tại!");
			return ResponseEntity.badRequest().body(result);
		}

		if (!email.equals(storedEmail)) {
			result.put("error", "Email không khớp với OTP đã gửi!");
			return ResponseEntity.badRequest().body(result);
		}

		if (!userOtp.equals(storedOtp)) {
			result.put("error", "Mã OTP không hợp lệ!");
			return ResponseEntity.badRequest().body(result);
		}

		// OTP đúng → xoá OTP khỏi session
		session.removeAttribute("otp");
		session.removeAttribute("otpEmail");

		// Lưu email đã xác thực OTP
		session.setAttribute("verifiedEmail", email);

		result.put("message", "Xác thực OTP thành công.");
		result.put("email", email); // Trả lại email đã xác thực
		return ResponseEntity.ok(result);
	}

	// Hàm tạo OTP 6 chữ số
	private String generateOtp() {
		return String.format("%06d", new Random().nextInt(1000000));
	}
	
}
