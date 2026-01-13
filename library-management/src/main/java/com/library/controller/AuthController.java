package com.library.controller;

import com.library.annotation.Log;
import com.library.annotation.Log.OperationType;
import com.library.common.Result;
import com.library.dto.request.ChangePasswordRequest;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.dto.response.UserResponse;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "认证管理", description = "用户登录、注册、获取当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户登录", description = "通过用户名和密码登录，返回 JWT Token。此接口无需认证。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功，返回 Token"),
            @ApiResponse(responseCode = "400", description = "用户名或密码错误",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "403", description = "账号已被禁用或已锁定",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "认证管理", operation = OperationType.LOGIN, description = "用户登录", saveResult = false)
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Parameter(description = "登录请求体", required = true)
            @Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "用户注册", description = "注册新用户账号。此接口无需认证。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "用户名已存在或参数校验失败",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "认证管理", operation = OperationType.CREATE, description = "用户注册")
    @PostMapping("/register")
    public Result<UserResponse> register(
            @Parameter(description = "注册请求体", required = true)
            @Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "获取当前用户", description = "获取当前登录用户的信息")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未登录或 Token 过期",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "旧密码错误或参数校验失败",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "401", description = "未登录或 Token 过期",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "认证管理", operation = OperationType.UPDATE, description = "修改密码", saveResult = false)
    @PostMapping("/change-password")
    public Result<Void> changePassword(
            @Parameter(description = "修改密码请求体", required = true)
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return Result.success();
    }
}
