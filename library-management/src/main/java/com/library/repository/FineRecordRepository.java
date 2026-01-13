package com.library.repository;

import com.library.entity.FineRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 罚款记录数据访问层
 */
@Repository
public interface FineRecordRepository extends JpaRepository<FineRecord, Long> {

    /**
     * 根据用户ID查询罚款记录
     */
    @Query("SELECT f FROM FineRecord f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.borrowRecord br LEFT JOIN FETCH br.book " +
            "WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    Page<FineRecord> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 根据借阅记录ID查询罚款记录
     */
    @Query("SELECT f FROM FineRecord f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.borrowRecord br LEFT JOIN FETCH br.book " +
            "WHERE f.borrowRecord.id = :borrowId")
    Optional<FineRecord> findByBorrowRecordId(@Param("borrowId") Long borrowId);

    /**
     * 查询所有罚款记录（分页）
     */
    @Query("SELECT f FROM FineRecord f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.borrowRecord br LEFT JOIN FETCH br.book " +
            "ORDER BY f.createdAt DESC")
    Page<FineRecord> findAllWithDetails(Pageable pageable);

    /**
     * 按状态查询罚款记录（分页）
     */
    @Query("SELECT f FROM FineRecord f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.borrowRecord br LEFT JOIN FETCH br.book " +
            "WHERE f.status = :status ORDER BY f.createdAt DESC")
    Page<FineRecord> findByStatus(@Param("status") FineRecord.Status status, Pageable pageable);

    /**
     * 按用户ID和状态查询罚款记录
     */
    @Query("SELECT f FROM FineRecord f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.borrowRecord br LEFT JOIN FETCH br.book " +
            "WHERE f.user.id = :userId AND f.status = :status ORDER BY f.createdAt DESC")
    Page<FineRecord> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") FineRecord.Status status, Pageable pageable);

    /**
     * 查询用户未缴纳的罚款总额
     */
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FineRecord f WHERE f.user.id = :userId AND f.status = 'UNPAID'")
    BigDecimal sumUnpaidAmountByUserId(@Param("userId") Long userId);

    /**
     * 查询用户未缴纳的罚款数量
     */
    @Query("SELECT COUNT(f) FROM FineRecord f WHERE f.user.id = :userId AND f.status = 'UNPAID'")
    long countUnpaidByUserId(@Param("userId") Long userId);

    /**
     * 查询所有未缴纳的罚款总额
     */
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FineRecord f WHERE f.status = 'UNPAID'")
    BigDecimal sumAllUnpaidAmount();

    /**
     * 统计各状态的罚款数量
     */
    @Query("SELECT f.status, COUNT(f) FROM FineRecord f GROUP BY f.status")
    List<Object[]> countByStatus();

    /**
     * 根据ID查询（带详情）
     */
    @Query("SELECT f FROM FineRecord f LEFT JOIN FETCH f.user LEFT JOIN FETCH f.borrowRecord br LEFT JOIN FETCH br.book " +
            "WHERE f.id = :id")
    Optional<FineRecord> findByIdWithDetails(@Param("id") Long id);

    /**
     * 条件查询（分页）
     */
    @Query("SELECT f FROM FineRecord f LEFT JOIN FETCH f.user u LEFT JOIN FETCH f.borrowRecord br LEFT JOIN FETCH br.book " +
            "WHERE (:userId IS NULL OR f.user.id = :userId) " +
            "AND (:status IS NULL OR f.status = :status) " +
            "AND (:username IS NULL OR u.username LIKE %:username%) " +
            "ORDER BY f.createdAt DESC")
    Page<FineRecord> findByConditions(
            @Param("userId") Long userId,
            @Param("status") FineRecord.Status status,
            @Param("username") String username,
            Pageable pageable);
}
