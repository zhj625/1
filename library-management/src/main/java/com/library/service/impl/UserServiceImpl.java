package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.common.PageResult;
import com.library.dto.request.ChangePasswordRequest;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.dto.response.UserResponse;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.UserRepository;
import com.library.security.JwtTokenProvider;
import com.library.service.LoginAttemptService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService;

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();

        // 1. 检查是否被锁定（登录失败次数限制）
        if (loginAttemptService.isBlocked(username)) {
            long remainingSeconds = loginAttemptService.getRemainingLockTimeSeconds(username);
            long remainingMinutes = (remainingSeconds + 59) / 60; // 向上取整
            log.warn("用户 {} 账号已锁定，剩余 {} 分钟", username, remainingMinutes);
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED,
                    "账号已锁定，请在 " + remainingMinutes + " 分钟后重试");
        }

        // 2. 先检查账号是否存在及状态（在密码验证前检查）
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            if (user.isPending()) {
                log.warn("用户 {} 账号待审核，拒绝登录", username);
                throw new BusinessException(ErrorCode.ACCOUNT_PENDING);
            }
            if (user.isDisabled()) {
                log.warn("用户 {} 账号已被禁用，拒绝登录", username);
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用，请联系管理员");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenProvider.generateToken(authentication);

            // 重新获取用户信息（确保数据最新）
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 登录成功，清除失败记录
            loginAttemptService.loginSucceeded(username);
            log.info("用户 {} 登录成功", username);

            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTime() / 1000)
                    .user(UserResponse.fromEntity(user))
                    .build();

        } catch (BadCredentialsException e) {
            // 登录失败，记录失败次数
            loginAttemptService.loginFailed(username);
            int remaining = loginAttemptService.getRemainingAttempts(username);

            if (remaining > 0) {
                throw new BusinessException(ErrorCode.LOGIN_FAILED,
                        "用户名或密码错误，还剩 " + remaining + " 次尝试机会");
            } else {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED,
                        "登录失败次数过多，账号已锁定 " + loginAttemptService.getLockTimeMinutes() + " 分钟");
            }
        }
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 解析角色：如果前端传了有效角色就使用，否则默认 USER
        User.Role role = User.Role.USER;
        boolean isAdminCreated = false;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                role = User.Role.valueOf(request.getRole().toUpperCase());
                isAdminCreated = true; // 管理员创建的用户
            } catch (IllegalArgumentException e) {
                // 无效角色值，使用默认 USER
                log.warn("无效的角色值: {}，使用默认角色 USER", request.getRole());
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(role)
                .status(isAdminCreated ? User.STATUS_ENABLED : User.STATUS_PENDING)
                .build();

        user = userRepository.save(user);
        log.info("用户注册成功: username={}, role={}, status={}", user.getUsername(), user.getRole(), user.getStatusText());
        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse getCurrentUser() {
        return UserResponse.fromEntity(getCurrentUserEntity());
    }

    @Override
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public PageResult<UserResponse> getUsers(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findByKeyword(keyword, pageRequest);

        return PageResult.of(
                userPage.getContent().stream()
                        .map(UserResponse::fromEntity)
                        .collect(Collectors.toList()),
                userPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 获取当前登录用户
        User currentUser = getCurrentUserEntity();

        // 保护措施1：不能禁用自己
        if (currentUser.getId().equals(id)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不能禁用自己的账号");
        }

        // 保护措施2：系统的初始超级管理员 admin 不能被任何人禁用
        if (status == 0 && "admin".equals(user.getUsername())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "不能禁用系统初始超级管理员账号");
        }

        // 保护措施3：普通管理员(ADMIN)只能由超级管理员 admin 禁用，不能互相禁用
        if (status == 0 && user.getRole() == User.Role.ADMIN && !"admin".equals(currentUser.getUsername())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "只有系统初始超级管理员可以禁用其他普通管理员账号");
        }

        // 保护措施4：馆员不能禁用其他馆员
        if (status == 0 && currentUser.getRole() == User.Role.LIBRARIAN && user.getRole() == User.Role.LIBRARIAN) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "馆员不能禁用其他馆员账号");
        }

        user.setStatus(status);
        userRepository.save(user);
        String statusText = switch (status) {
            case User.STATUS_DISABLED -> "禁用";
            case User.STATUS_ENABLED -> "启用";
            case User.STATUS_PENDING -> "待审核";
            default -> "未知";
        };
        log.info("用户 {} 的状态已被 {} 更新为 {}", user.getUsername(), currentUser.getUsername(), statusText);
    }

    @Override
    @Transactional
    public void approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isPending()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该用户不是待审核状态");
        }

        User currentUser = getCurrentUserEntity();
        user.setStatus(User.STATUS_ENABLED);
        userRepository.save(user);
        log.info("管理员 {} 审核通过用户 {}", currentUser.getUsername(), user.getUsername());
    }

    @Override
    @Transactional
    public void rejectUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isPending()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该用户不是待审核状态");
        }

        User currentUser = getCurrentUserEntity();
        // 拒绝注册直接删除用户
        userRepository.delete(user);
        log.info("管理员 {} 拒绝用户 {} 的注册申请", currentUser.getUsername(), user.getUsername());
    }

    @Override
    public PageResult<UserResponse> getPendingUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<User> userPage = userRepository.findByStatus(User.STATUS_PENDING, pageRequest);

        return PageResult.of(
                userPage.getContent().stream()
                        .map(UserResponse::fromEntity)
                        .collect(Collectors.toList()),
                userPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User currentUser = getCurrentUserEntity();

        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "不能删除系统超级管理员账号");
        }

        if (user.getRole() == User.Role.ADMIN && !"admin".equals(currentUser.getUsername())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "只有超级管理员可以删除其他普通管理员账号");
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        // 获取当前登录用户
        User user = getCurrentUserEntity();

        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "两次输入的密码不一致");
        }

        // 验证旧密码是否正确
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "旧密码错误");
        }

        // 验证新密码不能与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "新密码不能与旧密码相同");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("用户 {} 修改密码成功", user.getUsername());
    }
}
