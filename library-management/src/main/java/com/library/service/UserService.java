package com.library.service;

import com.library.common.PageResult;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.dto.response.UserResponse;
import com.library.entity.User;

public interface UserService {

    LoginResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    UserResponse getCurrentUser();

    User getCurrentUserEntity();

    PageResult<UserResponse> getUsers(String keyword, int page, int size);

    UserResponse getUserById(Long id);

    void updateUserStatus(Long id, Integer status);

    void deleteUser(Long id);
}
