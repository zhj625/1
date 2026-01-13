package com.library.controller;

import com.library.annotation.Log;
import com.library.annotation.Log.OperationType;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.FineRuleRequest;
import com.library.dto.request.WaiveFineRequest;
import com.library.dto.response.FineRecordResponse;
import com.library.dto.response.FineRuleResponse;
import com.library.service.FineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 罚款管理控制器
 */
@Tag(name = "罚款管理", description = "罚款规则配置和罚款记录管理")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    // ========== 罚款规则接口 ==========

    @Operation(summary = "获取罚款规则", description = "获取当前生效的罚款规则配置")
    @GetMapping("/config/fine-rules")
    public Result<FineRuleResponse> getFineRules() {
        return Result.success(fineService.getCurrentRule());
    }

    @Operation(summary = "更新罚款规则", description = "更新罚款规则配置（管理员权限）")
    @PutMapping("/config/fine-rules")
    @PreAuthorize("hasRole('ADMIN')")
    @Log(module = "罚款管理", operation = OperationType.UPDATE, description = "更新罚款规则配置")
    public Result<FineRuleResponse> updateFineRules(@Valid @RequestBody FineRuleRequest request) {
        return Result.success(fineService.updateRule(request));
    }

    // ========== 管理端罚款记录接口 ==========

    @Operation(summary = "查询罚款记录列表", description = "管理端查询所有罚款记录（管理员/馆员权限）")
    @GetMapping("/fines")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<FineRecordResponse>> getFineRecords(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "状态：UNPAID/PAID/WAIVED") @RequestParam(required = false) String status,
            @Parameter(description = "用户名（模糊搜索）") @RequestParam(required = false) String username,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(fineService.getFineRecords(userId, status, username, page, size));
    }

    @Operation(summary = "获取罚款记录详情", description = "根据ID获取罚款记录详情")
    @GetMapping("/fines/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<FineRecordResponse> getFineRecordById(
            @Parameter(description = "罚款记录ID") @PathVariable Long id) {
        return Result.success(fineService.getFineRecordById(id));
    }

    @Operation(summary = "免除罚款", description = "管理员免除用户罚款")
    @PostMapping("/fines/{id}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    @Log(module = "罚款管理", operation = OperationType.UPDATE, description = "管理员免除用户罚款")
    public Result<FineRecordResponse> waiveFine(
            @Parameter(description = "罚款记录ID") @PathVariable Long id,
            @Valid @RequestBody WaiveFineRequest request) {
        return Result.success(fineService.waiveFine(id, request.getReason()));
    }

    @Operation(summary = "获取罚款统计", description = "获取罚款统计信息（管理员/馆员权限）")
    @GetMapping("/fines/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Map<String, Object>> getFineStatistics() {
        return Result.success(fineService.getFineStatistics());
    }

    // ========== 用户端罚款记录接口 ==========

    @Operation(summary = "查询我的罚款记录", description = "查询当前用户的罚款记录")
    @GetMapping("/fines/me")
    public Result<PageResult<FineRecordResponse>> getMyFineRecords(
            @Parameter(description = "状态：UNPAID/PAID/WAIVED") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(fineService.getMyFineRecords(status, page, size));
    }

    @Operation(summary = "获取我的未缴罚款总额", description = "获取当前用户未缴纳的罚款总金额")
    @GetMapping("/fines/me/unpaid-amount")
    public Result<BigDecimal> getMyUnpaidAmount() {
        return Result.success(fineService.getMyUnpaidAmount());
    }

    @Operation(summary = "缴纳罚款", description = "缴纳指定的罚款记录")
    @PostMapping("/fines/{id}/pay")
    @Log(module = "罚款管理", operation = OperationType.UPDATE, description = "用户缴纳罚款")
    public Result<FineRecordResponse> payFine(
            @Parameter(description = "罚款记录ID") @PathVariable Long id) {
        return Result.success(fineService.payFine(id));
    }
}
