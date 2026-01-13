package com.library.controller;

import com.library.annotation.Log;
import com.library.annotation.Log.OperationType;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.BorrowQueryRequest;
import com.library.dto.request.BorrowRequest;
import com.library.dto.response.BorrowRecordResponse;
import com.library.service.BorrowService;
import com.library.service.ExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "借阅管理", description = "图书借阅、归还、记录查询")
@Slf4j
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final ExcelExportService excelExportService;

    @Operation(summary = "借阅图书", description = "用户借阅一本图书")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "借阅成功"),
            @ApiResponse(responseCode = "400", description = "库存不足/已借阅该书/超过借阅上限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "404", description = "图书不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "借阅管理", operation = OperationType.CREATE, description = "借阅图书")
    @PostMapping
    public Result<BorrowRecordResponse> borrowBook(
            @Parameter(description = "借阅请求", required = true)
            @Valid @RequestBody BorrowRequest request) {
        return Result.success(borrowService.borrowBook(request));
    }

    @Operation(summary = "归还图书", description = "归还已借阅的图书")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "归还成功"),
            @ApiResponse(responseCode = "400", description = "图书已归还",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "404", description = "借阅记录不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "借阅管理", operation = OperationType.UPDATE, description = "归还图书")
    @PostMapping("/{id}/return")
    public Result<BorrowRecordResponse> returnBook(
            @Parameter(description = "借阅记录ID", required = true) @PathVariable Long id) {
        return Result.success(borrowService.returnBook(id));
    }

    @Operation(summary = "续借图书", description = "续借图书，延长还书日期30天（最多续借2次）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "续借成功"),
            @ApiResponse(responseCode = "400", description = "不满足续借条件（已逾期/已达续借上限/已归还）",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "404", description = "借阅记录不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "借阅管理", operation = OperationType.UPDATE, description = "续借图书")
    @PostMapping("/{id}/renew")
    public Result<BorrowRecordResponse> renewBook(
            @Parameter(description = "借阅记录ID", required = true) @PathVariable Long id) {
        return Result.success(borrowService.renewBook(id));
    }

    @Operation(summary = "缴纳罚款", description = "缴纳逾期罚款")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "缴费成功"),
            @ApiResponse(responseCode = "400", description = "无需缴费/已缴费",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "404", description = "借阅记录不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "借阅管理", operation = OperationType.UPDATE, description = "缴纳罚款")
    @PostMapping("/{id}/pay-fine")
    public Result<BorrowRecordResponse> payFine(
            @Parameter(description = "借阅记录ID", required = true) @PathVariable Long id) {
        return Result.success(borrowService.payFine(id));
    }

    @Operation(summary = "获取借阅记录详情", description = "根据ID获取借阅记录")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "记录不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/{id}")
    public Result<BorrowRecordResponse> getRecordById(
            @Parameter(description = "借阅记录ID", required = true) @PathVariable Long id) {
        return Result.success(borrowService.getRecordById(id));
    }

    @Operation(summary = "分页查询借阅记录", description = "管理员/馆员查询所有借阅记录（需要管理员或馆员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<PageResult<BorrowRecordResponse>> getRecords(@ParameterObject BorrowQueryRequest request) {
        return Result.success(borrowService.getRecords(request));
    }

    @Operation(summary = "获取我的借阅记录", description = "获取当前用户的借阅记录")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping("/my")
    public Result<PageResult<BorrowRecordResponse>> getMyRecords(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(borrowService.getMyRecords(page, size));
    }

    @Operation(summary = "导出借阅记录", description = "导出借阅记录为 Excel 文件（需要管理员或馆员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "导出成功，返回 Excel 文件",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            schema = @Schema(type = "string", format = "binary", description = "Excel 文件二进制流"))),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "500", description = "导出失败",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "借阅管理", operation = OperationType.EXPORT, description = "导出借阅记录", saveResult = false)
    @GetMapping(value = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public void exportRecords(@ParameterObject BorrowQueryRequest request, HttpServletResponse response) {
        try {
            log.info("开始导出借阅记录, 查询条件: userId={}, bookId={}, status={}",
                    request.getUserId(), request.getBookId(), request.getStatus());
            List<BorrowRecordResponse> records = borrowService.getAllRecordsForExport(request);
            log.info("查询到 {} 条记录", records.size());
            excelExportService.exportBorrowRecords(records, response);
            log.info("导出完成");
        } catch (Exception e) {
            log.error("导出借阅记录失败", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }
}
