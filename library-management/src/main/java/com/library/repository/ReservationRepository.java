package com.library.repository;

import com.library.entity.Book;
import com.library.entity.Reservation;
import com.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 查询用户对某本书是否有有效预约（等待中或已通知）
     */
    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND r.book = :book " +
           "AND r.status IN (com.library.entity.Reservation$Status.WAITING, " +
           "com.library.entity.Reservation$Status.NOTIFIED)")
    Optional<Reservation> findActiveReservation(@Param("user") User user, @Param("book") Book book);

    /**
     * 统计某本书当前等待中的预约数量
     */
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.book = :book " +
           "AND r.status = com.library.entity.Reservation$Status.WAITING")
    int countWaitingByBook(@Param("book") Book book);

    /**
     * 查询某本书的第一位等待者
     */
    @Query("SELECT r FROM Reservation r WHERE r.book = :book " +
           "AND r.status = com.library.entity.Reservation$Status.WAITING " +
           "ORDER BY r.queuePosition ASC")
    List<Reservation> findFirstWaitingByBook(@Param("book") Book book, Pageable pageable);

    /**
     * 查询某本书所有等待中的预约（按队列位置排序）
     */
    @Query("SELECT r FROM Reservation r WHERE r.book = :book " +
           "AND r.status = com.library.entity.Reservation$Status.WAITING " +
           "ORDER BY r.queuePosition ASC")
    List<Reservation> findAllWaitingByBook(@Param("book") Book book);

    /**
     * 查询用户的所有预约记录
     */
    @Query("SELECT r FROM Reservation r WHERE r.user = :user ORDER BY r.createdAt DESC")
    Page<Reservation> findByUser(@Param("user") User user, Pageable pageable);

    /**
     * 查询用户的有效预约（等待中或已通知）
     */
    @Query("SELECT r FROM Reservation r WHERE r.user = :user " +
           "AND r.status IN (com.library.entity.Reservation$Status.WAITING, " +
           "com.library.entity.Reservation$Status.NOTIFIED) ORDER BY r.createdAt DESC")
    List<Reservation> findActiveByUser(@Param("user") User user);

    /**
     * 查询所有已过期的已通知预约（超过 expiresAt 仍未借阅）
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = com.library.entity.Reservation$Status.NOTIFIED " +
           "AND r.expiresAt < :now")
    List<Reservation> findExpiredNotifiedReservations(@Param("now") LocalDateTime now);

    /**
     * 将指定图书的等待中预约队列位置减1（用于取消预约后更新后续位置）
     */
    @Modifying
    @Query("UPDATE Reservation r SET r.queuePosition = r.queuePosition - 1 " +
           "WHERE r.book = :book AND r.status = com.library.entity.Reservation$Status.WAITING " +
           "AND r.queuePosition > :position")
    void decrementQueuePositionsAfter(@Param("book") Book book, @Param("position") int position);

    /**
     * 查询用户对某本书的已通知状态的预约
     */
    @Query("SELECT r FROM Reservation r WHERE r.user = :user AND r.book = :book " +
           "AND r.status = com.library.entity.Reservation$Status.NOTIFIED")
    Optional<Reservation> findNotifiedReservation(@Param("user") User user, @Param("book") Book book);
}
