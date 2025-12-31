package com.library.repository;

import com.library.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.user JOIN FETCH br.book WHERE (:userId IS NULL OR br.user.id = :userId) AND (:bookId IS NULL OR br.book.id = :bookId) AND (:status IS NULL OR br.status = :status)")
    Page<BorrowRecord> findByConditions(@Param("userId") Long userId, @Param("bookId") Long bookId, @Param("status") BorrowRecord.Status status, Pageable pageable);

    @Query("SELECT br FROM BorrowRecord br JOIN FETCH br.book WHERE br.user.id = :userId ORDER BY br.borrowDate DESC")
    Page<BorrowRecord> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.user.id = :userId AND br.status = com.library.entity.BorrowRecord$Status.BORROWING")
    long countActiveBorrowsByUserId(@Param("userId") Long userId);

    @Query("SELECT br FROM BorrowRecord br WHERE br.user.id = :userId AND br.book.id = :bookId AND br.status = com.library.entity.BorrowRecord$Status.BORROWING")
    Optional<BorrowRecord> findActiveBorrow(@Param("userId") Long userId, @Param("bookId") Long bookId);

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
}
