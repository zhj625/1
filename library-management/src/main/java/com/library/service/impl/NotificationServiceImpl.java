package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.dto.response.NotificationResponse;
import com.library.entity.BorrowRecord;
import com.library.entity.Notification;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.NotificationRepository;
import com.library.repository.UserRepository;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;

    private static final int REMINDER_DAYS_BEFORE = 3; // 提前3天提醒

    @Override
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        Long userId = getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    public long getUnreadCount() {
        Long userId = getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Long userId = getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "通知不存在"));

        // 验证通知属于当前用户
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作此通知");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long userId = getCurrentUserId();
        int count = notificationRepository.markAllAsRead(userId);
        log.info("用户 {} 标记了 {} 条通知为已读", userId, count);
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, Notification.Type type, String title, String content, Long borrowRecordId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));

        // 检查是否已发送过相同类型的通知（针对同一借阅记录）
        if (borrowRecordId != null && notificationRepository.existsByUserIdAndBorrowRecordIdAndType(userId, borrowRecordId, type)) {
            log.debug("用户 {} 的借阅记录 {} 已发送过 {} 类型通知，跳过", userId, borrowRecordId, type);
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .borrowRecordId(borrowRecordId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("向用户 {} 发送通知: {}", userId, title);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 30 8 * * ?") // 每天早上8:30执行
    public void checkAndSendDueReminders() {
        log.info("开始检查即将到期的借阅记录并发送提醒...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(REMINDER_DAYS_BEFORE);

        List<BorrowRecord> dueSoonRecords = borrowRecordRepository.findDueSoonRecords(now, deadline);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        int count = 0;
        for (BorrowRecord record : dueSoonRecords) {
            String title = "图书即将到期提醒";
            String content = String.format("您借阅的图书《%s》将于 %s 到期，请及时归还或续借。",
                    record.getBook().getTitle(),
                    record.getDueDate().format(formatter));

            sendNotification(record.getUser().getId(), Notification.Type.DUE_REMINDER,
                    title, content, record.getId());
            count++;
        }

        log.info("到期提醒发送完成，共发送 {} 条提醒", count);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 9 * * ?") // 每天早上9:00执行
    public void checkAndSendOverdueNotices() {
        log.info("开始检查逾期记录并发送通知...");

        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(now);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int count = 0;
        for (BorrowRecord record : overdueRecords) {
            int overdueDays = record.calculateOverdueDays();
            String title = "图书逾期通知";
            String content = String.format("您借阅的图书《%s》已逾期 %d 天，请尽快归还。逾期罚款：%.2f 元/天。",
                    record.getBook().getTitle(),
                    overdueDays,
                    0.5);

            sendNotification(record.getUser().getId(), Notification.Type.OVERDUE_NOTICE,
                    title, content, record.getId());
            count++;
        }

        log.info("逾期通知发送完成，共发送 {} 条通知", count);
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录"))
                .getId();
    }
}
