package com.library.service;

import com.library.dto.request.ReviewRequest;
import com.library.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReviewService {

    /**
     * 添加评论（当前用户）
     */
    ReviewResponse addReview(ReviewRequest request);

    /**
     * 获取图书的评论列表
     */
    Page<ReviewResponse> getBookReviews(Long bookId, int page, int size);

    /**
     * 获取当前用户的评论列表
     */
    List<ReviewResponse> getMyReviews();

    /**
     * 获取所有评论（管理后台）
     */
    Page<ReviewResponse> getAllReviews(String keyword, int page, int size);

    /**
     * 删除评论（用户删除自己的）
     */
    void deleteReview(Long reviewId);

    /**
     * 删除评论（管理员）
     */
    void adminDeleteReview(Long reviewId);

    /**
     * 点赞评论
     */
    void likeReview(Long reviewId);

    /**
     * 更新评论状态（隐藏/显示）
     */
    void updateReviewStatus(Long reviewId, Integer status);

    /**
     * 获取最新评论（首页展示）
     */
    List<ReviewResponse> getLatestReviews(int limit);

    /**
     * 获取图书的平均评分
     */
    Double getBookAverageRating(Long bookId);

    /**
     * 统计图书的评论数
     */
    long getBookReviewCount(Long bookId);
}
