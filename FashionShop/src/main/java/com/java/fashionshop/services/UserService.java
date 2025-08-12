package com.java.fashionshop.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.java.fashionshop.bean.RegisterBean;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaUser;

@Service
public class UserService {

    @Autowired
    private JpaUser userJPA;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Đăng ký tài khoản mới
    public UserEntity registerUser(RegisterBean registerBean) {
        UserEntity user = new UserEntity();
        user.setEmail(registerBean.getEmail());

        // ✅ Mã hóa mật khẩu bằng BCrypt (dùng PasswordEncoder)
        String hashedPassword = passwordEncoder.encode(registerBean.getPassword());
        user.setPassword(hashedPassword);

        user.setFullName(registerBean.getFullName());
        user.setDateCreated(LocalDateTime.now());
        user.setStatus(true);
        user.setRole(1); 
        user.setAvatar(null);
        user.setOtp(null);
        user.setOtpExpiry(null);
        return userJPA.save(user);
    }

    // Kiểm tra email đã tồn tại
    public boolean isEmailExists(String email) {
        return userJPA.findByEmail(email).isPresent();
    }

    // Tìm user theo email
    public Optional<UserEntity> findByEmail(String email) {
        return userJPA.findByEmail(email);
    }

    public boolean isEmailExistsForOtherUsers(String email, Integer currentUserId) {
        Optional<UserEntity> userOpt = userJPA.findByEmail(email);
        return userOpt.isPresent() && !userOpt.get().getUserId().equals(currentUserId);
    }

    // Lưu user (update hoặc thêm mới)
    public UserEntity save(UserEntity user) {
        return userJPA.save(user);
    }

    // Tìm user theo ID
    public Optional<UserEntity> findById(Integer id) {
        return userJPA.findById(id);
    }

    public UserEntity findUserByEmail(String email) {
        Optional<UserEntity> userOpt = userJPA.findByEmail(email);
        return userOpt.orElse(null); // Trả về null nếu không có
    }
    // Xóa user theo ID
    public void deleteById(Integer id) {
        userJPA.deleteById(id);
        
    }

    // Lấy tất cả người dùng (trừ role = 0)
    public List<UserEntity> findAllUsers() {
        List<UserEntity> users = userJPA.findAll();
        return users.stream()
                    .filter(user -> user.getRole() != 0)
                    .collect(Collectors.toList());
    }
    
    public Page<UserEntity> findUsersPaged(int page, int size, String name, String email, LocalDateTime fromDate, LocalDateTime toDate, Boolean status) {
        Pageable pageable = PageRequest.of(page, size);  // Không sắp xếp theo status nữa, sử dụng mặc định
        return userJPA.findUsersPagedWithFilters(name, email, fromDate, toDate, status, pageable);
    }

    public UserEntity updateUserStatus(Integer userId, boolean status) {
        Optional<UserEntity> userOpt = userJPA.findById(userId);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setStatus(status);
            return userJPA.save(user);
        }
        return null;
    }
    
    
}
