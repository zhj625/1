package com.library.controller;

import com.library.common.Result;
import com.library.dto.request.ReviewRequest;
import com.library.dto.response.ReviewResponse;
import com.library.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "评论管理", description = "图书评论相关接口")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "添加评论", description = "用户对图书进行评论")
    @PostMapping
    public Result<ReviewResponse> addReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.addReview(request);
        return Result.success(response);
    }

    @Operation(summary = "获取图书评论", description = "获取指定图书的评论列表")
    @GetMapping("/book/{bookId}")
    public Result<Map<String, Object>> getBookReviews(
            @Parameter(description = "图书ID") @PathVariable Long bookId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<ReviewResponse> reviews = reviewService.getBookReviews(bookId, page, size);
        Double avgRating = reviewService.getBookAverageRating(bookId);
        long reviewCount = reviewService.getBookReviewCount(bookId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", reviews.getContent());
        result.put("total", reviews.getTotalElements());
        result.put("pages", reviews.getTotalPages());
        result.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0);
        result.put("reviewCount", reviewCount);
        return Result.success(result);
    }

    @Operation(summary = "获取用户评论", description = "获取当前用户的评论列表")
    @GetMapping("/my")
    public Result<List<ReviewResponse>> getMyReviews() {
        List<ReviewResponse> reviews = reviewService.getMyReviews();
        return Result.success(reviews);
    }

    @Operation(summary = "获取最新评论", description = "获取最新评论用于首页展示")
    @GetMapping("/latest")
    public Result<List<ReviewResponse>> getLatestReviews(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "6") int limit) {
        List<ReviewResponse> reviews = reviewService.getLatestReviews(limit);
        return Result.success(reviews);
    }

    @Operation(summary = "删除评论", description = "用户删除自己的评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@Parameter(description = "评论ID") @PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success(null);
    }

    @Operation(summary = "点赞评论", description = "为评论点赞")
    @PostMapping("/{id}/like")
    public Result<Void> likeReview(@Parameter(description = "评论ID") @PathVariable Long id) {
        reviewService.likeReview(id);
        return Result.success(null);
    }

    // ========== 管理员接口 ==========

    @Operation(summary = "获取所有评论（管理）", description = "管理员获取所有评论列表")
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Map<String, Object>> getAllReviews(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Page<ReviewResponse> reviews = reviewService.getAllReviews(keyword, page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("list", reviews.getContent());
        result.put("total", reviews.getTotalElements());
        result.put("pages", reviews.getTotalPages());
        return Result.success(result);
    }

    @Operation(summary = "管理员删除评论", description = "管理员删除任意评论")
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> adminDeleteReview(@Parameter(description = "评论ID") @PathVariable Long id) {
        reviewService.adminDeleteReview(id);
        return Result.success(null);
    }

    @Operation(summary = "更新评论状态", description = "管理员隐藏/显示评论")
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public Result<Void> updateReviewStatus(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Parameter(description = "状态: 0-隐藏, 1-显示") @RequestParam Integer status) {
        reviewService.updateReviewStatus(id, status);
        return Result.success(null);
    }
}
