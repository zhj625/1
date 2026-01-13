package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.common.PageResult;
import com.library.dto.response.ReservationResponse;
import com.library.entity.Book;
import com.library.entity.Notification;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.service.NotificationService;
import com.library.service.ReservationService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * 预约优先借阅有效期（天）
     */
    private static final int RESERVATION_PRIORITY_DAYS = 3;

    @Override
    @Transactional
    public ReservationResponse reserveBook(Long bookId) {
        User user = userService.getCurrentUserEntity();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // 检查图书是否有库存（有库存不能预约）
        if (book.getAvailableCount() > 0) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_ALLOWED);
        }

        // 检查用户是否已有该书的有效预约
        if (reservationRepository.findActiveReservation(user, book).isPresent()) {
            throw new BusinessException(ErrorCode.RESERVATION_EXISTS);
        }

        // 获取当前队列长度，确定位置
        int queuePosition = reservationRepository.countWaitingByBook(book) + 1;

        // 创建预约
        Reservation reservation = Reservation.builder()
                .user(user)
                .book(book)
                .status(Reservation.Status.WAITING)
                .queuePosition(queuePosition)
                .build();

        reservation = reservationRepository.save(reservation);
        log.info("用户 {} 预约图书《{}》成功，队列位置: {}", user.getUsername(), book.getTitle(), queuePosition);

        return ReservationResponse.fromEntity(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        User user = userService.getCurrentUserEntity();
        if (!reservation.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.RESERVATION_PERMISSION_DENIED);
        }

        // 只有等待中或已通知状态可以取消
        if (reservation.getStatus() != Reservation.Status.WAITING &&
            reservation.getStatus() != Reservation.Status.NOTIFIED) {
            throw new BusinessException(ErrorCode.RESERVATION_CANCEL_NOT_ALLOWED);
        }

        int oldPosition = reservation.getQueuePosition();
        Book book = reservation.getBook();

        // 更新状态为已取消
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservationRepository.save(reservation);

        // 如果是等待中的预约被取消，需要更新后续位置
        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            reservationRepository.decrementQueuePositionsAfter(book, oldPosition);
        }

        log.info("用户 {} 取消预约图书《{}》", user.getUsername(), book.getTitle());
    }

    @Override
    public PageResult<ReservationResponse> getMyReservations(int page, int size) {
        User user = userService.getCurrentUserEntity();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Reservation> reservationPage = reservationRepository.findByUser(user, pageable);

        return PageResult.of(
                reservationPage.getContent().stream()
                        .map(ReservationResponse::fromEntity)
                        .collect(Collectors.toList()),
                reservationPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public List<ReservationResponse> getMyActiveReservations() {
        User user = userService.getCurrentUserEntity();
        return reservationRepository.findActiveByUser(user).stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasActiveReservation(Long bookId) {
        User user = userService.getCurrentUserEntity();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        return reservationRepository.findActiveReservation(user, book).isPresent();
    }

    @Override
    public int getQueueLength(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        return reservationRepository.countWaitingByBook(book);
    }

    @Override
    @Transactional
    public void notifyNextReservation(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // 查询该书第一位等待者
        List<Reservation> waitingList = reservationRepository.findFirstWaitingByBook(book, PageRequest.of(0, 1));
        if (waitingList.isEmpty()) {
            return;
        }

        Reservation reservation = waitingList.get(0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(RESERVATION_PRIORITY_DAYS);

        // 更新预约状态
        reservation.setStatus(Reservation.Status.NOTIFIED);
        reservation.setNotifiedAt(now);
        reservation.setExpiresAt(expiresAt);
        reservationRepository.save(reservation);

        // 发送通知
        notificationService.sendNotification(
                reservation.getUser().getId(),
                Notification.Type.BOOK_AVAILABLE,
                "预约图书可借阅",
                String.format("您预约的图书《%s》已可借阅，请在 %d 天内前往借阅，逾期将失效。",
                        book.getTitle(), RESERVATION_PRIORITY_DAYS),
                null
        );

        log.info("已通知用户 {} 预约的图书《{}》可借阅，有效期至 {}",
                reservation.getUser().getUsername(), book.getTitle(), expiresAt);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void processExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredList = reservationRepository.findExpiredNotifiedReservations(now);

        for (Reservation reservation : expiredList) {
            // 标记为过期
            reservation.setStatus(Reservation.Status.EXPIRED);
            reservationRepository.save(reservation);

            // 发送过期通知
            notificationService.sendNotification(
                    reservation.getUser().getId(),
                    Notification.Type.RESERVATION_EXPIRED,
                    "预约已过期",
                    String.format("您预约的图书《%s》因未在规定时间内借阅已过期。如需借阅，请重新预约。",
                            reservation.getBook().getTitle()),
                    null
            );

            log.info("预约过期处理：用户 {} 预约的图书《{}》已过期",
                    reservation.getUser().getUsername(), reservation.getBook().getTitle());

            // 通知下一位等待者
            notifyNextReservation(reservation.getBook().getId());
        }

        if (!expiredList.isEmpty()) {
            log.info("本次处理过期预约 {} 条", expiredList.size());
        }
    }

    @Override
    @Transactional
    public void fulfillReservation(Long userId, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        User user = userService.getCurrentUserEntity();

        // 查找该用户的已通知预约
        reservationRepository.findNotifiedReservation(user, book).ifPresent(reservation -> {
            reservation.setStatus(Reservation.Status.FULFILLED);
            reservationRepository.save(reservation);
            log.info("用户 {} 完成预约借阅《{}》", user.getUsername(), book.getTitle());
        });
    }
}
