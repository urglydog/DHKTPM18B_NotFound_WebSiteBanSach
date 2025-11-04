package com.notfound.bookstore.service;

public interface UserService {
    boolean existsByEmail(String email);

    void resetPassword(String email, String newPassword);
}
