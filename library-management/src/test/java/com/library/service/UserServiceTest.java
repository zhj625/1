package com.library.service;

import com.library.common.ErrorCode;
import com.library.dto.request.ChangePasswordRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.UserResponse;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.UserRepository;
import com.library.security.JwtTokenProvider;
import com.library.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 * 使用 JUnit 5 + Mockito 测试核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRealName("测试用户");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.USER);
        testUser.setStatus(User.STATUS_ENABLED);

        adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", 2L);
        adminUser.setUsername("admin");
        adminUser.setPassword("encodedPassword");
        adminUser.setRealName("管理员");
        adminUser.setRole(User.Role.ADMIN);
        adminUser.setStatus(User.STATUS_ENABLED);
    }

    @Test
    @DisplayName("注册成功 - 新用户为待审核状态")
    void register_Success_PendingStatus() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setRealName("新用户");
        request.setEmail("new@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            ReflectionTestUtils.setField(u, "id", 3L);
            return u;
        });

        // When
        UserResponse response = userService.register(request);

        // Then
        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals(User.STATUS_PENDING, response.getStatus());
        assertEquals("待审核", response.getStatusText());
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void register_UsernameExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.register(request));

        assertEquals(ErrorCode.USERNAME_EXISTS.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("审核通过用户")
    void approveUser_Success() {
        // Given
        User pendingUser = new User();
        ReflectionTestUtils.setField(pendingUser, "id", 3L);
        pendingUser.setUsername("pendinguser");
        pendingUser.setStatus(User.STATUS_PENDING);
        pendingUser.setRole(User.Role.USER);

        mockCurrentUser(adminUser); // 模拟管理员操作
        when(userRepository.findById(3L)).thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class))).thenReturn(pendingUser);

        // When
        assertDoesNotThrow(() -> userService.approveUser(3L));

        // Then
        assertEquals(User.STATUS_ENABLED, pendingUser.getStatus());
    }

    @Test
    @DisplayName("审核通过失败 - 用户不是待审核状态")
    void approveUser_NotPending() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser)); // 已启用状态

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.approveUser(1L));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("拒绝用户注册")
    void rejectUser_Success() {
        // Given
        User pendingUser = new User();
        ReflectionTestUtils.setField(pendingUser, "id", 3L);
        pendingUser.setUsername("pendinguser");
        pendingUser.setStatus(User.STATUS_PENDING);
        pendingUser.setRole(User.Role.USER);

        mockCurrentUser(adminUser); // 模拟管理员操作
        when(userRepository.findById(3L)).thenReturn(Optional.of(pendingUser));

        // When
        assertDoesNotThrow(() -> userService.rejectUser(3L));

        // Then
        verify(userRepository).delete(pendingUser);
    }

    @Test
    @DisplayName("修改密码成功")
    void changePassword_Success() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        mockCurrentUser(testUser);
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then
        assertDoesNotThrow(() -> userService.changePassword(request));

        // Verify
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("修改密码失败 - 两次密码不一致")
    void changePassword_PasswordMismatch() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("differentPassword");

        mockCurrentUser(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.changePassword(request));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("不一致"));
    }

    @Test
    @DisplayName("修改密码失败 - 旧密码错误")
    void changePassword_WrongOldPassword() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");
        request.setConfirmPassword("newPassword");

        mockCurrentUser(testUser);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.changePassword(request));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("旧密码"));
    }

    @Test
    @DisplayName("修改密码失败 - 新旧密码相同")
    void changePassword_SamePassword() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("samePassword");
        request.setNewPassword("samePassword");
        request.setConfirmPassword("samePassword");

        mockCurrentUser(testUser);
        when(passwordEncoder.matches("samePassword", "encodedPassword")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.changePassword(request));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("不能与旧密码相同"));
    }

    @Test
    @DisplayName("禁用用户 - 不能禁用自己")
    void updateUserStatus_CannotDisableSelf() {
        // Given
        mockCurrentUser(adminUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateUserStatus(2L, User.STATUS_DISABLED));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("不能禁用自己"));
    }

    @Test
    @DisplayName("禁用用户 - 不能禁用管理员")
    void updateUserStatus_CannotDisableAdmin() {
        // Given
        User anotherAdmin = new User();
        ReflectionTestUtils.setField(anotherAdmin, "id", 3L);
        anotherAdmin.setUsername("anotheradmin");
        anotherAdmin.setRole(User.Role.ADMIN);
        anotherAdmin.setStatus(User.STATUS_ENABLED);

        mockCurrentUser(adminUser);
        when(userRepository.findById(3L)).thenReturn(Optional.of(anotherAdmin));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userService.updateUserStatus(3L, User.STATUS_DISABLED));

        assertEquals(ErrorCode.NO_PERMISSION.getCode(), exception.getCode());
    }

    private void mockCurrentUser(User user) {
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn(user.getUsername());
        lenient().when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
        lenient().when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }
}
