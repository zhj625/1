package com.library.repository;

import com.library.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    /**
     * 使用悲观锁查询图书（用于借阅/归还时锁定库存）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    /**
     * 原子性减少可借数量（返回影响行数，0表示库存不足）
     */
    @Modifying
    @Query("UPDATE Book b SET b.availableCount = b.availableCount - 1, b.version = b.version + 1 " +
           "WHERE b.id = :id AND b.availableCount > 0 AND b.status = 1")
    int decreaseAvailableCount(@Param("id") Long id);

    /**
     * 原子性增加可借数量
     */
    @Modifying
    @Query("UPDATE Book b SET b.availableCount = b.availableCount + 1, b.version = b.version + 1 " +
           "WHERE b.id = :id AND b.availableCount < b.totalCount")
    int increaseAvailableCount(@Param("id") Long id);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE (:keyword IS NULL OR :keyword = '' OR b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%) AND (:categoryId IS NULL OR b.category.id = :categoryId) AND (:status IS NULL OR b.status = :status)")
    Page<Book> findByConditions(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("status") Integer status, Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE b.id = :id")
    Optional<Book> findByIdWithCategory(@Param("id") Long id);

    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    long countByCategoryId(Long categoryId);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE (:keyword IS NULL OR :keyword = '' OR b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%) AND (:categoryId IS NULL OR b.category.id = :categoryId) AND (:author IS NULL OR :author = '' OR b.author LIKE %:author%) AND (:publisher IS NULL OR :publisher = '' OR b.publisher LIKE %:publisher%) AND (:minPrice IS NULL OR b.price >= :minPrice) AND (:maxPrice IS NULL OR b.price <= :maxPrice) AND (:status IS NULL OR b.status = :status)")
    Page<Book> advancedSearch(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("author") String author, @Param("publisher") String publisher, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, @Param("status") Integer status, Pageable pageable);

    /**
     * 检查图书是否有未归还的借阅记录
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.book.id = :bookId AND br.status <> com.library.entity.BorrowRecord$Status.RETURNED")
    long countActiveBorrowsByBookId(@Param("bookId") Long bookId);

    /**
     * 获取新书推荐（最近N天内入库的图书）
     */
    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE b.createdAt >= :since AND b.status = 1 ORDER BY b.createdAt DESC")
    List<Book> findNewArrivals(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 获取热门图书（按借阅次数排序）
     */
    @Query("SELECT b, COUNT(br) as borrowCount FROM Book b LEFT JOIN FETCH b.category LEFT JOIN BorrowRecord br ON br.book = b WHERE b.status = 1 GROUP BY b ORDER BY COUNT(br) DESC")
    List<Book> findPopularBooks(Pageable pageable);
}
