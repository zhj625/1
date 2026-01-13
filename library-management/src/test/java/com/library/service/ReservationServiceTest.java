package com.library.service;

import com.library.common.ErrorCode;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 预约服务单元测试
 * 使用 JUnit 5 + Mockito 测试核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("预约服务测试")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User testUser;
    private Book testBook;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 初始化测试分类
        testCategory = new Category();
        ReflectionTestUtils.setField(testCategory, "id", 1L);
        testCategory.setName("测试分类");

        // 初始化测试用户
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setRole(User.Role.USER);
        testUser.setStatus(User.STATUS_ENABLED);

        // 初始化测试图书（无库存才能预约）
        testBook = new Book();
        ReflectionTestUtils.setField(testBook, "id", 1L);
        testBook.setTitle("测试图书");
        testBook.setAuthor("测试作者");
        testBook.setIsbn("978-7-111-12345-1");
        testBook.setTotalCount(5);
        testBook.setAvailableCount(0); // 无库存
        testBook.setStatus(1);
        testBook.setCategory(testCategory);
        testBook.setPrice(new BigDecimal("29.99"));
    }

    @Test
    @DisplayName("预约成功 - 图书无库存")
    void reserveBook_Success() {
        // Given
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reservationRepository.findActiveReservation(testUser, testBook)).thenReturn(Optional.empty());
        when(reservationRepository.countWaitingByBook(testBook)).thenReturn(2);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation r = invocation.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });

        // When
        var response = reservationService.reserveBook(1L);

        // Then
        assertNotNull(response);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("预约失败 - 图书不存在")
    void reserveBook_BookNotFound() {
        // Given
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.reserveBook(999L));

        assertEquals(ErrorCode.BOOK_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("预约失败 - 图书有库存无需预约")
    void reserveBook_BookAvailable() {
        // Given
        testBook.setAvailableCount(3); // 有库存
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.reserveBook(1L));

        assertEquals(ErrorCode.RESERVATION_NOT_ALLOWED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("预约失败 - 已有预约")
    void reserveBook_AlreadyReserved() {
        // Given
        Reservation existingReservation = new Reservation();
        existingReservation.setUser(testUser);
        existingReservation.setBook(testBook);
        existingReservation.setStatus(Reservation.Status.WAITING);

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reservationRepository.findActiveReservation(testUser, testBook)).thenReturn(Optional.of(existingReservation));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.reserveBook(1L));

        assertEquals(ErrorCode.RESERVATION_EXISTS.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("取消预约成功")
    void cancelReservation_Success() {
        // Given
        Reservation reservation = new Reservation();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        reservation.setUser(testUser);
        reservation.setBook(testBook);
        reservation.setStatus(Reservation.Status.WAITING);
        reservation.setQueuePosition(1);

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        assertDoesNotThrow(() -> reservationService.cancelReservation(1L));

        // Verify
        assertEquals(Reservation.Status.CANCELLED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    @DisplayName("取消预约失败 - 预约不存在")
    void cancelReservation_NotFound() {
        // Given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.cancelReservation(999L));

        assertEquals(ErrorCode.RESERVATION_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("取消预约失败 - 非本人预约")
    void cancelReservation_PermissionDenied() {
        // Given
        User otherUser = new User();
        ReflectionTestUtils.setField(otherUser, "id", 2L);
        otherUser.setUsername("otheruser");

        Reservation reservation = new Reservation();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        reservation.setUser(otherUser); // 其他用户的预约
        reservation.setBook(testBook);
        reservation.setStatus(Reservation.Status.WAITING);

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.cancelReservation(1L));

        assertEquals(ErrorCode.RESERVATION_PERMISSION_DENIED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("取消预约失败 - 状态不允许取消")
    void cancelReservation_StatusNotAllowed() {
        // Given
        Reservation reservation = new Reservation();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        reservation.setUser(testUser);
        reservation.setBook(testBook);
        reservation.setStatus(Reservation.Status.FULFILLED); // 已完成

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reservationService.cancelReservation(1L));

        assertEquals(ErrorCode.RESERVATION_CANCEL_NOT_ALLOWED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("检查用户是否已预约 - 有预约")
    void hasActiveReservation_True() {
        // Given
        Reservation activeReservation = new Reservation();
        activeReservation.setStatus(Reservation.Status.WAITING);

        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reservationRepository.findActiveReservation(testUser, testBook)).thenReturn(Optional.of(activeReservation));

        // When
        boolean result = reservationService.hasActiveReservation(1L);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("检查用户是否已预约 - 无预约")
    void hasActiveReservation_False() {
        // Given
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reservationRepository.findActiveReservation(testUser, testBook)).thenReturn(Optional.empty());

        // When
        boolean result = reservationService.hasActiveReservation(1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("获取预约队列长度")
    void getQueueLength() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(reservationRepository.countWaitingByBook(testBook)).thenReturn(5);

        // When
        int length = reservationService.getQueueLength(1L);

        // Then
        assertEquals(5, length);
    }
}
