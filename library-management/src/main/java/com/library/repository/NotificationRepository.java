package com.library.repository;

import com.library.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 查询用户的所有通知（分页，按时间倒序）
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 查询用户的未读通知
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * 统计用户未读通知数量
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 将用户所有未读通知标记为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 检查是否已经发送过某类型的通知（防止重复发送）
     */
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.user.id = :userId AND n.borrowRecordId = :borrowRecordId AND n.type = :type")
    boolean existsByUserIdAndBorrowRecordIdAndType(@Param("userId") Long userId,
                                                    @Param("borrowRecordId") Long borrowRecordId,
                                                    @Param("type") Notification.Type type);

    /**
     * 删除用户的所有通知
     */
    void deleteByUserId(Long userId);
}
