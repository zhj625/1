package com.library.service;

import com.library.common.ErrorCode;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Category;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.service.impl.BorrowServiceImpl;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 借阅服务单元测试
 * 使用 JUnit 5 + Mockito 测试核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("借阅服务测试")
class BorrowServiceTest {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserService userService;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private User testUser;
    private Book testBook;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 设置配置参数
        ReflectionTestUtils.setField(borrowService, "maxBorrowCount", 5);
        ReflectionTestUtils.setField(borrowService, "minBorrowDays", 1);
        ReflectionTestUtils.setField(borrowService, "maxBorrowDays", 90);
        ReflectionTestUtils.setField(borrowService, "maxRenewCount", 2);
        ReflectionTestUtils.setField(borrowService, "renewDays", 30);
        ReflectionTestUtils.setField(borrowService, "finePerDay", new BigDecimal("0.50"));

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

        // 初始化测试图书
        testBook = new Book();
        ReflectionTestUtils.setField(testBook, "id", 1L);
        testBook.setTitle("测试图书");
        testBook.setAuthor("测试作者");
        testBook.setIsbn("978-7-111-12345-1");
        testBook.setTotalCount(5);
        testBook.setAvailableCount(3);
        testBook.setStatus(1);
        testBook.setCategory(testCategory);
        testBook.setPrice(new BigDecimal("29.99"));
    }

    @Test
    @DisplayName("还书成功 - 正常归还")
    void returnBook_Success() {
        // Given
        BorrowRecord record = new BorrowRecord();
        ReflectionTestUtils.setField(record, "id", 1L);
        record.setUser(testUser);
        record.setBook(testBook);
        record.setStatus(BorrowRecord.Status.BORROWING);
        record.setBorrowDate(LocalDateTime.now().minusDays(10));
        record.setDueDate(LocalDateTime.now().plusDays(20));
        record.setRenewCount(0);
        record.setOverdueDays(0);
        record.setFineAmount(BigDecimal.ZERO);
        record.setFinePaid(false);

        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(bookRepository.increaseAvailableCount(1L)).thenReturn(1);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(record);

        // When
        var response = borrowService.returnBook(1L);

        // Then
        assertNotNull(response);
        assertEquals(BorrowRecord.Status.RETURNED, record.getStatus());
        assertNotNull(record.getReturnDate());
        verify(bookRepository).increaseAvailableCount(1L);
    }

    @Test
    @DisplayName("还书失败 - 记录不存在")
    void returnBook_RecordNotFound() {
        // Given
        when(borrowRecordRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> borrowService.returnBook(999L));

        assertEquals(ErrorCode.BORROW_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("还书失败 - 已归还")
    void returnBook_AlreadyReturned() {
        // Given
        BorrowRecord record = new BorrowRecord();
        ReflectionTestUtils.setField(record, "id", 1L);
        record.setUser(testUser);
        record.setBook(testBook);
        record.setStatus(BorrowRecord.Status.RETURNED);

        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));
        when(userService.getCurrentUserEntity()).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> borrowService.returnBook(1L));

        assertEquals(ErrorCode.ALREADY_RETURNED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("续借成功")
    void renewBook_Success() {
        // Given
        BorrowRecord record = new BorrowRecord();
        ReflectionTestUtils.setField(record, "id", 1L);
        record.setUser(testUser);
        record.setBook(testBook);
        record.setStatus(BorrowRecord.Status.BORROWING);
        record.setBorrowDate(LocalDateTime.now().minusDays(10));
        record.setDueDate(LocalDateTime.now().plusDays(5));
        record.setRenewCount(0);
        record.setOverdueDays(0);
        record.setFineAmount(BigDecimal.ZERO);
        record.setFinePaid(false);

        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));
        when(userService.getCurrentUserEntity()).thenReturn(testUser);
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenReturn(record);

        // When
        var response = borrowService.renewBook(1L);

        // Then
        assertNotNull(response);
        assertEquals(1, record.getRenewCount());
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    @DisplayName("续借失败 - 超过最大续借次数")
    void renewBook_LimitExceeded() {
        // Given
        BorrowRecord record = new BorrowRecord();
        ReflectionTestUtils.setField(record, "id", 1L);
        record.setUser(testUser);
        record.setBook(testBook);
        record.setStatus(BorrowRecord.Status.BORROWING);
        record.setDueDate(LocalDateTime.now().plusDays(5));
        record.setRenewCount(2); // 已续借2次
        record.setOverdueDays(0);
        record.setFineAmount(BigDecimal.ZERO);
        record.setFinePaid(false);

        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));
        when(userService.getCurrentUserEntity()).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> borrowService.renewBook(1L));

        assertEquals(ErrorCode.RENEW_LIMIT_EXCEEDED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("续借失败 - 逾期图书不能续借")
    void renewBook_OverdueNotAllowed() {
        // Given
        BorrowRecord record = new BorrowRecord();
        ReflectionTestUtils.setField(record, "id", 1L);
        record.setUser(testUser);
        record.setBook(testBook);
        record.setStatus(BorrowRecord.Status.OVERDUE); // 已逾期
        record.setDueDate(LocalDateTime.now().minusDays(5));
        record.setRenewCount(0);
        record.setOverdueDays(5);
        record.setFineAmount(new BigDecimal("2.50"));
        record.setFinePaid(false);

        when(borrowRecordRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(record));
        when(userService.getCurrentUserEntity()).thenReturn(testUser);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> borrowService.renewBook(1L));

        assertEquals(ErrorCode.RENEW_OVERDUE_NOT_ALLOWED.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("续借失败 - 记录不存在")
    void renewBook_RecordNotFound() {
        // Given
        when(borrowRecordRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> borrowService.renewBook(999L));

        assertEquals(ErrorCode.BORROW_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("获取借阅记录 - 记录存在")
    void getRecordById_Success() {
        // Given
        BorrowRecord record = new BorrowRecord();
        ReflectionTestUtils.setField(record, "id", 1L);
        record.setUser(testUser);
        record.setBook(testBook);
        record.setStatus(BorrowRecord.Status.BORROWING);
        record.setBorrowDate(LocalDateTime.now().minusDays(10));
        record.setDueDate(LocalDateTime.now().plusDays(20));
        record.setRenewCount(0);
        record.setOverdueDays(0);
        record.setFineAmount(BigDecimal.ZERO);
        record.setFinePaid(false);

        when(borrowRecordRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(record));

        // When
        var response = borrowService.getRecordById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("获取借阅记录 - 记录不存在")
    void getRecordById_NotFound() {
        // Given
        when(borrowRecordRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> borrowService.getRecordById(999L));

        assertEquals(ErrorCode.BORROW_NOT_FOUND.getCode(), exception.getCode());
    }
}
