package com.library.repository;

import com.library.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 根据图书ID查询评论（分页，只显示状态为1的）
     */
    Page<Review> findByBook_IdAndStatusOrderByCreatedAtDesc(Long bookId, Integer status, Pageable pageable);

    /**
     * 查询所有评论（管理后台，分页）
     */
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据用户ID查询评论
     */
    List<Review> findByUser_IdAndStatusOrderByCreatedAtDesc(Long userId, Integer status);

    /**
     * 检查用户是否已对某本书评论过
     */
    boolean existsByUser_IdAndBook_Id(Long userId, Long bookId);

    /**
     * 根据用户ID和图书ID查询评论
     */
    Optional<Review> findByUser_IdAndBook_Id(Long userId, Long bookId);

    /**
     * 点赞数+1
     */
    @Modifying
    @Query("UPDATE Review r SET r.likes = r.likes + 1 WHERE r.id = :id")
    void incrementLikes(@Param("id") Long id);

    /**
     * 获取图书的平均评分
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId AND r.status = 1")
    Double getAverageRatingByBookId(@Param("bookId") Long bookId);

    /**
     * 统计图书的评论数
     */
    long countByBook_IdAndStatus(Long bookId, Integer status);

    /**
     * 获取最新评论（首页展示）
     */
    @Query("SELECT r FROM Review r WHERE r.status = 1 ORDER BY r.createdAt DESC")
    List<Review> findLatestReviews(Pageable pageable);

    /**
     * 搜索评论（管理后台）
     */
    @Query("SELECT r FROM Review r WHERE " +
           "(:keyword IS NULL OR r.content LIKE %:keyword% OR r.user.username LIKE %:keyword% OR r.book.title LIKE %:keyword%) " +
           "ORDER BY r.createdAt DESC")
    Page<Review> searchReviews(@Param("keyword") String keyword, Pageable pageable);
}
