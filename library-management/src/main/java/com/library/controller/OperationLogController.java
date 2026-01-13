package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.entity.OperationLog;
import com.library.repository.OperationLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "操作日志", description = "管理员操作日志查询")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OperationLogController {

    private final OperationLogRepository operationLogRepository;

    @Operation(summary = "分页查询操作日志", description = "管理员查询系统操作日志")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @GetMapping
    public Result<PageResult<OperationLog>> getLogs(
            @Parameter(description = "模块名称") @RequestParam(required = false) String module,
            @Parameter(description = "操作人") @RequestParam(required = false) String operator,
            @Parameter(description = "开始时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {

        page = page > 0 ? page : 1;
        size = size > 0 ? Math.min(size, 100) : 20;

        PageRequest pageRequest = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "operationTime"));

        Page<OperationLog> logPage = operationLogRepository.findByConditions(
                module, operator, startTime, endTime, pageRequest);

        return Result.success(PageResult.of(
                logPage.getContent(),
                logPage.getTotalElements(),
                page,
                size
        ));
    }

    @Operation(summary = "获取日志详情", description = "根据ID获取操作日志详情")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @GetMapping("/{id}")
    public Result<OperationLog> getLogById(
            @Parameter(description = "日志ID") @PathVariable Long id) {
        return operationLogRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error(404, "日志记录不存在"));
    }
}
