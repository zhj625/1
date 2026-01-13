package com.library.service;

import com.library.common.PageResult;
import com.library.dto.response.ReservationResponse;

public interface ReservationService {

    /**
     * 预约图书
     * @param bookId 图书ID
     * @return 预约信息
     */
    ReservationResponse reserveBook(Long bookId);

    /**
     * 取消预约
     * @param reservationId 预约ID
     */
    void cancelReservation(Long reservationId);

    /**
     * 获取当前用户的预约列表
     */
    PageResult<ReservationResponse> getMyReservations(int page, int size);

    /**
     * 获取当前用户的有效预约列表（等待中或已通知）
     */
    java.util.List<ReservationResponse> getMyActiveReservations();

    /**
     * 检查用户对某本书是否有有效预约
     */
    boolean hasActiveReservation(Long bookId);

    /**
     * 获取某本书的预约队列长度
     */
    int getQueueLength(Long bookId);

    /**
     * 处理图书归还后的预约通知（由归还接口调用）
     * @param bookId 归还的图书ID
     */
    void notifyNextReservation(Long bookId);

    /**
     * 处理过期的预约（定时任务调用）
     */
    void processExpiredReservations();

    /**
     * 完成预约（用户借阅成功后调用）
     */
    void fulfillReservation(Long userId, Long bookId);
}
