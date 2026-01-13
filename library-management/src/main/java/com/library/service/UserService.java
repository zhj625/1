package com.library.service;

import com.library.common.PageResult;
import com.library.dto.request.ChangePasswordRequest;
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

    /**
     * 修改当前用户密码
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 审核通过用户
     */
    void approveUser(Long id);

    /**
     * 拒绝用户注册
     */
    void rejectUser(Long id);

    /**
     * 获取待审核用户列表
     */
    PageResult<UserResponse> getPendingUsers(int page, int size);
}
