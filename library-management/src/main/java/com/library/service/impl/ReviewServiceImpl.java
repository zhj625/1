package com.library.service.impl;

import com.library.exception.BusinessException;
import com.library.common.ErrorCode;
import com.library.dto.request.ReviewRequest;
import com.library.dto.response.ReviewResponse;
import com.library.entity.Book;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.repository.BookRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.UserRepository;
import com.library.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request) {
        Long userId = getCurrentUserId();

        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 检查图书是否存在
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // 检查是否已评论过
        if (reviewRepository.existsByUser_IdAndBook_Id(userId, request.getBookId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "您已经评论过这本书了");
        }

        // 创建评论
        Review review = Review.builder()
                .user(user)
                .book(book)
                .rating(request.getRating())
                .content(request.getContent())
                .likes(0)
                .status(1)
                .build();

        review = reviewRepository.save(review);
        return ReviewResponse.fromEntity(review);
    }

    @Override
    public Page<ReviewResponse> getBookReviews(Long bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviews = reviewRepository.findByBook_IdAndStatusOrderByCreatedAtDesc(bookId, 1, pageable);
        return reviews.map(ReviewResponse::fromEntity);
    }

    @Override
    public List<ReviewResponse> getMyReviews() {
        Long userId = getCurrentUserId();
        List<Review> reviews = reviewRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, 1);
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewResponse> getAllReviews(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviews;
        if (keyword != null && !keyword.trim().isEmpty()) {
            reviews = reviewRepository.searchReviews(keyword.trim(), pageable);
        } else {
            reviews = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return reviews.map(ReviewResponse::fromEntity);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Long userId = getCurrentUserId();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "评论不存在"));

        // 检查是否是自己的评论
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "只能删除自己的评论");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional
    public void adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "评论不存在"));
        reviewRepository.delete(review);
    }

    @Override
    @Transactional
    public void likeReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评论不存在");
        }
        reviewRepository.incrementLikes(reviewId);
    }

    @Override
    @Transactional
    public void updateReviewStatus(Long reviewId, Integer status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "评论不存在"));
        review.setStatus(status);
        reviewRepository.save(review);
    }

    @Override
    public List<ReviewResponse> getLatestReviews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findLatestReviews(pageable);
        return reviews.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Double getBookAverageRating(Long bookId) {
        return reviewRepository.getAverageRatingByBookId(bookId);
    }

    @Override
    public long getBookReviewCount(Long bookId) {
        return reviewRepository.countByBook_IdAndStatus(bookId, 1);
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录"))
                .getId();
    }
}
