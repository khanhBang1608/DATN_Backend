//package com.java.fashionshop.controller;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseAuthException;
//import com.google.firebase.auth.FirebaseToken;
//
//import com.java.fashionshop.entity.UserEntity;
//import com.java.fashionshop.component.JwtUtil;
//import com.java.fashionshop.dto.LoginResponseDTO;
//import com.java.fashionshop.dto.UserDTO;
//import com.java.fashionshop.jpa.JpaUser;
//
//import jakarta.servlet.http.HttpServletResponse;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
//@RequestMapping("/api/public/oauth")
//public class OAuthController {
//
//    private final JpaUser userRepo;
//    private final JwtUtil jwtUtil;
//
//    public OAuthController(JpaUser userRepo, JwtUtil jwtUtil) {
//        this.userRepo = userRepo;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @PostMapping("/google")
//    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body, HttpServletResponse response) {
//        String idToken = body.get("idToken");
//
//        try {
//            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
//            String email = decodedToken.getEmail();
//            String name = decodedToken.getName();
//            String picture = decodedToken.getPicture();
//
//            // Kiểm tra user có tồn tại chưa
//            UserEntity user = userRepo.findByEmail(email).orElseGet(() -> {
//                UserEntity newUser = new UserEntity();
//                newUser.setEmail(email);
//                newUser.setFullName(name);
//                newUser.setAvatar(picture);
//                newUser.setPassword(""); // hoặc random
//                newUser.setStatus(true);
//                newUser.setRole(1); // mặc định là USER
//                return userRepo.save(newUser);
//            });
//
//            if (!user.getStatus()) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Tài khoản đã bị khóa");
//            }
//
//            String role = user.getRole() == 0?"ADMIN":"USER";
//            String token = jwtUtil.generateToken(email, role);
//
//            // Tạo DTO
//            UserDTO userDTO = new UserDTO(
//                user.getUserId(),
//                user.getFullName(),
//                user.getEmail(),
//                user.getAvatar(),
//                user.getStatus(),
//                user.getRole(),
//                user.getDateCreated()
//            );
//
////            // Lưu token vào cookie
////            Cookie cookie = new Cookie("token", token);
////            cookie.setHttpOnly(true);
////            cookie.setPath("/");
////            cookie.setMaxAge(2 * 60 * 60); // 2 giờ
////            response.addCookie(cookie);
//
//            return ResponseEntity.ok(new LoginResponseDTO(token, userDTO));
//
//        } catch (FirebaseAuthException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ");
//        }
//    }
//}
//
