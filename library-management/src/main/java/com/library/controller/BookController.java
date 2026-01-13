package com.library.controller;

import com.library.annotation.Log;
import com.library.annotation.Log.OperationType;
import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.BookQueryRequest;
import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import com.library.service.BookService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "图书管理", description = "图书的增删改查操作")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "新增图书", description = "添加一本新书到系统（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "ISBN已存在或参数校验失败",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "图书管理", operation = OperationType.CREATE, description = "新增图书")
    @PostMapping
    public Result<BookResponse> createBook(
            @Parameter(description = "图书信息", required = true)
            @Valid @RequestBody BookRequest request) {
        return Result.success(bookService.createBook(request));
    }

    @Operation(summary = "更新图书", description = "根据ID更新图书信息（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "404", description = "图书不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "图书管理", operation = OperationType.UPDATE, description = "修改图书")
    @PutMapping("/{id}")
    public Result<BookResponse> updateBook(
            @Parameter(description = "图书ID", required = true) @PathVariable Long id,
            @Parameter(description = "图书信息", required = true) @Valid @RequestBody BookRequest request) {
        return Result.success(bookService.updateBook(id, request));
    }

    @Operation(summary = "删除图书", description = "根据ID删除图书（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "图书不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @Log(module = "图书管理", operation = OperationType.DELETE, description = "删除图书")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(
            @Parameter(description = "图书ID", required = true) @PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @Operation(summary = "获取图书详情", description = "根据ID获取单本图书的详细信息。此接口无需认证。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "图书不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/{id}")
    public Result<BookResponse> getBookById(
            @Parameter(description = "图书ID", required = true) @PathVariable Long id) {
        return Result.success(bookService.getBookById(id));
    }

    @Operation(summary = "分页查询图书", description = "根据条件分页查询图书列表。此接口无需认证。")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping
    public Result<PageResult<BookResponse>> getBooks(@ParameterObject BookQueryRequest request) {
        return Result.success(bookService.getBooks(request));
    }

    @Operation(summary = "获取新书推荐", description = "获取最近入库的新书列表。此接口无需认证。")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping("/new-arrivals")
    public Result<List<BookResponse>> getNewArrivals(
            @Parameter(description = "查询天数范围，默认30天")
            @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "返回数量限制，默认10本")
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getNewArrivals(days, limit));
    }

    @Operation(summary = "获取热门图书", description = "获取借阅次数最多的热门图书列表。此接口无需认证。")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping("/popular")
    public Result<List<BookResponse>> getPopularBooks(
            @Parameter(description = "返回数量限制，默认10本")
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(bookService.getPopularBooks(limit));
    }
}
