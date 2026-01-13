package com.library.controller;

import com.library.annotation.Log;
import com.library.annotation.Log.OperationType;
import com.library.common.Result;
import com.library.dto.response.NotificationResponse;
import com.library.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "通知管理", description = "站内通知相关接口")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取我的通知列表", description = "分页获取当前用户的通知列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping
    public Result<Page<NotificationResponse>> getMyNotifications(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return Result.success(notificationService.getMyNotifications(pageable));
    }

    @Operation(summary = "获取未读通知数量", description = "获取当前用户的未读通知数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "标记单条通知为已读", description = "将指定通知标记为已读")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "标记成功"),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "404", description = "通知不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @PutMapping("/{id}/read")
    @Log(module = "通知", operation = OperationType.UPDATE, description = "标记通知为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success(null);
    }

    @Operation(summary = "标记所有通知为已读", description = "将当前用户的所有通知标记为已读")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "标记成功"),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @PutMapping("/read-all")
    @Log(module = "通知", operation = OperationType.UPDATE, description = "标记所有通知为已读")
    public Result<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return Result.success(null);
    }
}
