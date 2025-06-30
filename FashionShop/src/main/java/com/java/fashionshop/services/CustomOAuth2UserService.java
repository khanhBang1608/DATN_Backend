package com.java.fashionshop.services;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaUser;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private JpaUser userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String avatar = oauth2User.getAttribute("picture");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from Google account");
        }

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            UserEntity existingUser = userOpt.get();
            boolean updated = false;

            if (name != null && !name.equals(existingUser.getFullName())) {
                existingUser.setFullName(name);
                updated = true;
            }

            if (avatar != null && !avatar.equals(existingUser.getAvatar())) {
                existingUser.setAvatar(avatar);
                updated = true;
            }

            if (updated) {
                userRepository.save(existingUser);
            }
        } else {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setFullName(name != null ? name : "No Name");
            newUser.setAvatar(avatar);
            newUser.setPassword(UUID.randomUUID().toString());
            newUser.setRole(1); // 1 = USER
            newUser.setStatus(true);
            userRepository.save(newUser);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oauth2User.getAttributes(),
                "email"
        );
    }
}
