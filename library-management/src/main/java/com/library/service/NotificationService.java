package com.library.service;

import com.library.dto.response.NotificationResponse;
import com.library.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * 获取当前用户的通知列表（分页）
     */
    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    /**
     * 获取当前用户的未读通知数量
     */
    long getUnreadCount();

    /**
     * 标记单条通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead();

    /**
     * 发送通知
     */
    void sendNotification(Long userId, Notification.Type type, String title, String content, Long borrowRecordId);

    /**
     * 检查并发送到期提醒（提前3天提醒）
     */
    void checkAndSendDueReminders();

    /**
     * 检查并发送逾期通知
     */
    void checkAndSendOverdueNotices();
}
