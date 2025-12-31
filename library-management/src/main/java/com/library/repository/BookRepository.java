package com.library.repository;

import com.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE (:keyword IS NULL OR :keyword = '' OR b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%) AND (:categoryId IS NULL OR b.category.id = :categoryId) AND (:status IS NULL OR b.status = :status)")
    Page<Book> findByConditions(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("status") Integer status, Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE b.id = :id")
    Optional<Book> findByIdWithCategory(@Param("id") Long id);

    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    long countByCategoryId(Long categoryId);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.category WHERE (:keyword IS NULL OR :keyword = '' OR b.title LIKE %:keyword% OR b.author LIKE %:keyword% OR b.isbn LIKE %:keyword%) AND (:categoryId IS NULL OR b.category.id = :categoryId) AND (:author IS NULL OR :author = '' OR b.author LIKE %:author%) AND (:publisher IS NULL OR :publisher = '' OR b.publisher LIKE %:publisher%) AND (:minPrice IS NULL OR b.price >= :minPrice) AND (:maxPrice IS NULL OR b.price <= :maxPrice) AND (:status IS NULL OR b.status = :status)")
    Page<Book> advancedSearch(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("author") String author, @Param("publisher") String publisher, @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, @Param("status") Integer status, Pageable pageable);
}
