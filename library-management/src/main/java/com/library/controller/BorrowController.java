package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.BorrowQueryRequest;
import com.library.dto.request.BorrowRequest;
import com.library.dto.response.BorrowRecordResponse;
import com.library.service.BorrowService;
import com.library.service.ExcelExportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final ExcelExportService excelExportService;

    @PostMapping
    public Result<BorrowRecordResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        return Result.success(borrowService.borrowBook(request));
    }

    @PostMapping("/{id}/return")
    public Result<BorrowRecordResponse> returnBook(@PathVariable Long id) {
        return Result.success(borrowService.returnBook(id));
    }

    @GetMapping("/{id}")
    public Result<BorrowRecordResponse> getRecordById(@PathVariable Long id) {
        return Result.success(borrowService.getRecordById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<BorrowRecordResponse>> getRecords(BorrowQueryRequest request) {
        return Result.success(borrowService.getRecords(request));
    }

    @GetMapping("/my")
    public Result<PageResult<BorrowRecordResponse>> getMyRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(borrowService.getMyRecords(page, size));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportRecords(BorrowQueryRequest request, HttpServletResponse response) {
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
