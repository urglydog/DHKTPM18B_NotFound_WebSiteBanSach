package com.notfound.bookstore.service.impl;

import com.notfound.bookstore.exception.AppException;
import com.notfound.bookstore.exception.ErrorCode;
import com.notfound.bookstore.model.entity.User;
import com.notfound.bookstore.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements com.notfound.bookstore.service.UserService {

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void resetPassword(String email, String newPassword){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
