package com.library.controller;

import com.library.annotation.Log;
import com.library.annotation.Log.OperationType;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.response.ReservationResponse;
import com.library.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "预约管理", description = "图书预约相关接口")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "预约图书", description = "当图书库存为0时，用户可以预约该书")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "预约成功"),
            @ApiResponse(responseCode = "400", description = "图书有库存或已预约",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "预约管理", operation = OperationType.CREATE, description = "预约图书")
    @PostMapping("/{bookId}")
    public Result<ReservationResponse> reserveBook(
            @Parameter(description = "图书ID", required = true)
            @PathVariable Long bookId) {
        return Result.success(reservationService.reserveBook(bookId));
    }

    @Operation(summary = "取消预约", description = "取消等待中或已通知状态的预约")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "400", description = "预约不存在或状态不允许取消",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "预约管理", operation = OperationType.DELETE, description = "取消预约")
    @DeleteMapping("/{id}")
    public Result<Void> cancelReservation(
            @Parameter(description = "预约ID", required = true)
            @PathVariable Long id) {
        reservationService.cancelReservation(id);
        return Result.success();
    }

    @Operation(summary = "获取我的预约列表", description = "获取当前用户的所有预约记录（分页）")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/my")
    public Result<PageResult<ReservationResponse>> getMyReservations(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(reservationService.getMyReservations(page, size));
    }

    @Operation(summary = "获取我的有效预约", description = "获取当前用户的等待中或已通知的预约")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/my/active")
    public Result<List<ReservationResponse>> getMyActiveReservations() {
        return Result.success(reservationService.getMyActiveReservations());
    }

    @Operation(summary = "检查是否有有效预约", description = "检查当前用户对某本书是否有有效预约")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/check/{bookId}")
    public Result<Boolean> hasActiveReservation(
            @Parameter(description = "图书ID", required = true)
            @PathVariable Long bookId) {
        return Result.success(reservationService.hasActiveReservation(bookId));
    }

    @Operation(summary = "获取图书预约队列长度", description = "获取某本书当前的预约排队人数")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/queue/{bookId}")
    public Result<Map<String, Integer>> getQueueLength(
            @Parameter(description = "图书ID", required = true)
            @PathVariable Long bookId) {
        Map<String, Integer> result = new HashMap<>();
        result.put("queueLength", reservationService.getQueueLength(bookId));
        return Result.success(result);
    }
}
