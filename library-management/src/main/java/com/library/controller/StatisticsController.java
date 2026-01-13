package com.library.controller;

import com.library.common.Result;
import com.library.dto.request.StatisticsQueryRequest;
import com.library.dto.response.AdvancedStatisticsResponse;
import com.library.dto.response.StatisticsResponse;
import com.library.service.StatisticsService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "统计分析", description = "系统数据统计（需要管理员或馆员权限）")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取统计数据", description = "获取系统基础统计数据")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<StatisticsResponse> getStatistics() {
        return Result.success(statisticsService.getStatistics());
    }

    @Operation(summary = "获取仪表盘数据", description = "获取管理后台仪表盘展示数据，包含借阅趋势、热门图书等")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<StatisticsResponse> getDashboard() {
        return Result.success(statisticsService.getDashboardData());
    }

    @Operation(summary = "获取高级统计数据", description = "支持按分类、时间段、用户角色等多维度统计分析")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/advanced")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<AdvancedStatisticsResponse> getAdvancedStatistics(
            @Parameter(description = "统计查询条件") StatisticsQueryRequest request) {
        return Result.success(statisticsService.getAdvancedStatistics(request));
    }
}
