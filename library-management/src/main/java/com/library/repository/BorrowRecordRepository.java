package com.library.repository;

import com.library.entity.BorrowRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.user JOIN FETCH br.book WHERE br.id = :id")
    Optional<BorrowRecord> findByIdWithDetails(@Param("id") Long id);

    /**
     * 使用悲观锁查询借阅记录（用于归还时锁定）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.user JOIN FETCH br.book WHERE br.id = :id")
    Optional<BorrowRecord> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.user JOIN FETCH br.book WHERE (:userId IS NULL OR br.user.id = :userId) AND (:bookId IS NULL OR br.book.id = :bookId) AND (:status IS NULL OR br.status = :status)")
    Page<BorrowRecord> findByConditions(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("status") BorrowRecord.Status status, Pageable pageable);

    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.book WHERE br.user.id = :userId ORDER BY br.borrowDate DESC")
    Page<BorrowRecord> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 统计用户活跃借阅数量（包括 BORROWING 和 OVERDUE，两者都是未归还状态）
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.user.id = :userId AND br.status <> com.library.entity.BorrowRecord$Status.RETURNED")
    long countActiveBorrowsByUserId(@Param("userId") Long userId);

    /**
     * 查找用户对某本书的未归还借阅记录（包括 BORROWING 和 OVERDUE）
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.user.id = :userId AND br.book.id = :bookId AND br.status <> com.library.entity.BorrowRecord$Status.RETURNED")
    Optional<BorrowRecord> findActiveBorrow(@Param("userId") Long userId, @Param("bookId") Long bookId);

    /**
     * 使用悲观锁检查重复借阅（防止并发重复借阅，包括 BORROWING 和 OVERDUE）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT br FROM BorrowRecord br WHERE br.user.id = :userId AND br.book.id = :bookId AND br.status <> com.library.entity.BorrowRecord$Status.RETURNED")
    Optional<BorrowRecord> findActiveBorrowForUpdate(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Query("SELECT br FROM BorrowRecord br WHERE br.status = com.library.entity.BorrowRecord$Status.BORROWING AND br.dueDate < :now")
    List<BorrowRecord> findOverdueRecords(@Param("now") LocalDateTime now);

    long countByStatus(BorrowRecord.Status status);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.borrowDate BETWEEN :start AND :end")
    long countByBorrowDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.returnDate BETWEEN :start AND :end")
    long countByReturnDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT b.id, b.title, b.author, COUNT(br.id) as borrow_count FROM borrow_record br JOIN book b ON br.book_id = b.id GROUP BY b.id, b.title, b.author ORDER BY borrow_count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findHotBooks(@Param("limit") int limit);

    @Query(value = "SELECT u.id, u.username, COUNT(br.id) as borrow_count FROM borrow_record br JOIN user u ON br.user_id = u.id GROUP BY u.id, u.username ORDER BY borrow_count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findActiveUsers(@Param("limit") int limit);

    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.user JOIN FETCH br.book WHERE (:userId IS NULL OR br.user.id = :userId) AND (:bookId IS NULL OR br.book.id = :bookId) AND (:status IS NULL OR br.status = :status) ORDER BY br.borrowDate DESC")
    List<BorrowRecord> findAllByConditions(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("status") BorrowRecord.Status status);

    /**
     * 检查用户是否有未归还的借阅记录
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.user.id = :userId AND br.status <> com.library.entity.BorrowRecord$Status.RETURNED")
    long countActiveBorrowsByUserIdForDelete(@Param("userId") Long userId);

    /**
     * 查找即将到期的借阅记录（提前N天提醒）
     */
    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.user JOIN FETCH br.book WHERE br.status = com.library.entity.BorrowRecord$Status.BORROWING AND br.dueDate BETWEEN :now AND :deadline")
    List<BorrowRecord> findDueSoonRecords(@Param("now") LocalDateTime now, @Param("deadline") LocalDateTime deadline);

    /**
     * 按分类统计借阅数量
     */
    @Query(value = "SELECT c.name as categoryName, COUNT(br.id) as borrowCount " +
            "FROM borrow_record br " +
            "JOIN book b ON br.book_id = b.id " +
            "JOIN category c ON b.category_id = c.id " +
            "WHERE br.borrow_date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.id, c.name " +
            "ORDER BY borrowCount DESC", nativeQuery = true)
    List<Object[]> countByCategory(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 按时间段统计借阅数量（每月）
     */
    @Query(value = "SELECT DATE_FORMAT(br.borrow_date, '%Y-%m') as month, COUNT(br.id) as borrowCount " +
            "FROM borrow_record br " +
            "WHERE br.borrow_date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE_FORMAT(br.borrow_date, '%Y-%m') " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> countByMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 按用户角色统计借阅数量
     */
    @Query(value = "SELECT u.role as userRole, COUNT(br.id) as borrowCount " +
            "FROM borrow_record br " +
            "JOIN user u ON br.user_id = u.id " +
            "WHERE br.borrow_date BETWEEN :startDate AND :endDate " +
            "GROUP BY u.role", nativeQuery = true)
    List<Object[]> countByUserRole(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 按时间段统计归还数量（每月）
     */
    @Query(value = "SELECT DATE_FORMAT(br.return_date, '%Y-%m') as month, COUNT(br.id) as returnCount " +
            "FROM borrow_record br " +
            "WHERE br.return_date IS NOT NULL AND br.return_date BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE_FORMAT(br.return_date, '%Y-%m') " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> countReturnsByMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
