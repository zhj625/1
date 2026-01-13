package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.response.FavoriteResponse;
import com.library.service.FavoriteService;
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

import java.util.Map;

import static com.library.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@Tag(name = "收藏管理", description = "图书收藏相关操作")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "添加收藏", description = "将图书添加到收藏夹")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "收藏成功"),
            @ApiResponse(responseCode = "400", description = "已收藏该图书",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "401", description = "未认证",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult"))),
            @ApiResponse(responseCode = "404", description = "图书不存在",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @PostMapping("/{bookId}")
    public Result<FavoriteResponse> addFavorite(
            @Parameter(description = "图书ID", required = true) @PathVariable Long bookId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "备注信息（可选）",
                    content = @Content(schema = @Schema(example = "{\"remark\": \"很喜欢这本书\"}")))
            @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        return Result.success(favoriteService.addFavorite(bookId, remark));
    }

    @Operation(summary = "取消收藏", description = "将图书从收藏夹移除")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "取消成功"),
            @ApiResponse(responseCode = "404", description = "未收藏该图书",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResult")))
    })
    @DeleteMapping("/{bookId}")
    public Result<Void> removeFavorite(
            @Parameter(description = "图书ID", required = true) @PathVariable Long bookId) {
        favoriteService.removeFavorite(bookId);
        return Result.success();
    }

    @Operation(summary = "获取我的收藏", description = "分页获取当前用户的收藏列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping
    public Result<PageResult<FavoriteResponse>> getMyFavorites(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(favoriteService.getMyFavorites(page, size));
    }

    @Operation(summary = "检查是否收藏", description = "检查当前用户是否收藏了指定图书")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @GetMapping("/check/{bookId}")
    public Result<Boolean> checkFavorite(
            @Parameter(description = "图书ID", required = true) @PathVariable Long bookId) {
        return Result.success(favoriteService.isFavorited(bookId));
    }

    @Operation(summary = "获取收藏数量", description = "获取指定图书的收藏总数")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping("/count/{bookId}")
    public Result<Long> getFavoriteCount(
            @Parameter(description = "图书ID", required = true) @PathVariable Long bookId) {
        return Result.success(favoriteService.getFavoriteCount(bookId));
    }
}
