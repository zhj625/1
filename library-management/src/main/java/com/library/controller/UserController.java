package com.library.controller;

import com.library.annotation.Log;
import com.library.annotation.Log.OperationType;
import com.library.common.PageResult;
import com.library.common.Result;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "用户管理", description = "用户的增删改查操作（需要管理员权限）")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户", description = "根据关键词分页查询用户列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping
    public Result<PageResult<UserResponse>> getUsers(
            @Parameter(description = "搜索关键词（用户名/真实姓名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.getUsers(keyword, page, size));
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "更新用户状态", description = "启用或禁用用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "用户管理", operation = OperationType.UPDATE, description = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "状态：1-启用，0-禁用", required = true) @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "用户管理", operation = OperationType.DELETE, description = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "获取待审核用户列表", description = "分页查询待审核的用户列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/pending")
    public Result<PageResult<UserResponse>> getPendingUsers(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.getPendingUsers(page, size));
    }

    @Operation(summary = "审核通过用户", description = "审核通过用户的注册申请")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "审核通过成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "400", description = "用户状态不正确",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "用户管理", operation = OperationType.UPDATE, description = "审核通过用户")
    @PostMapping("/{id}/approve")
    public Result<Void> approveUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.approveUser(id);
        return Result.success();
    }

    @Operation(summary = "拒绝用户注册", description = "拒绝用户的注册申请")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "拒绝成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "400", description = "用户状态不正确",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "用户管理", operation = OperationType.DELETE, description = "拒绝用户注册")
    @PostMapping("/{id}/reject")
    public Result<Void> rejectUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        userService.rejectUser(id);
        return Result.success();
    }
}
