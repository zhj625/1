package com.library.controller;

import com.library.common.Result;
import com.library.dto.request.CategoryRequest;
import com.library.dto.response.CategoryResponse;
import com.library.service.CategoryService;
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

import java.util.List;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "分类管理", description = "图书分类的增删改查操作")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "新增分类", description = "添加一个新的图书分类（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "分类名已存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "403", description = "无权限",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @PostMapping
    public Result<CategoryResponse> createCategory(
            @Parameter(description = "分类信息", required = true)
            @Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.createCategory(request));
    }

    @Operation(summary = "更新分类", description = "根据ID更新分类信息（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "分类不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @PutMapping("/{id}")
    public Result<CategoryResponse> updateCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id,
            @Parameter(description = "分类信息", required = true) @Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "删除分类", description = "根据ID删除分类（需要管理员权限）")
    @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "分类下有图书，无法删除",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "404", description = "分类不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "获取分类详情", description = "根据ID获取分类信息。此接口无需认证。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "分类不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategoryById(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @Operation(summary = "获取所有分类", description = "获取所有分类列表。此接口无需认证。")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping
    public Result<List<CategoryResponse>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @Operation(summary = "获取分类树", description = "获取树形结构的分类列表。此接口无需认证。")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/tree")
    public Result<List<CategoryResponse>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }
}
