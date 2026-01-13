package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.common.PageResult;
import com.library.dto.request.BorrowQueryRequest;
import com.library.dto.request.BorrowRequest;
import com.library.dto.response.BorrowRecordResponse;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.service.BorrowService;
import com.library.service.FineService;
import com.library.service.ReservationService;
import com.library.service.UserService;
import com.library.entity.FineRecord;
import com.library.entity.FineRule;
import com.library.repository.FineRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserService userService;
    private final FineRecordRepository fineRecordRepository;

    // 使用 @Lazy 避免循环依赖
    private ReservationService reservationService;
    private FineService fineService;

    @Autowired
    public void setReservationService(@Lazy ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Autowired
    public void setFineService(@Lazy FineService fineService) {
        this.fineService = fineService;
    }

    @Value("${library.max-borrow-count:5}")
    private int maxBorrowCount;

    @Value("${library.min-borrow-days:1}")
    private int minBorrowDays;

    @Value("${library.max-borrow-days:90}")
    private int maxBorrowDays;

    @Value("${library.max-renew-count:2}")
    private int maxRenewCount;

    @Value("${library.renew-days:30}")
    private int renewDays;

    @Value("${library.fine-per-day:0.50}")
    private BigDecimal finePerDay;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BorrowRecordResponse borrowBook(BorrowRequest request) {
        User user = userService.getCurrentUserEntity();

        // 1. 校验借阅天数
        int days = request.getDays() != null ? request.getDays() : 30;
        if (days < minBorrowDays || days > maxBorrowDays) {
            throw new BusinessException(ErrorCode.BORROW_DAYS_INVALID,
                    "借阅天数必须在" + minBorrowDays + "-" + maxBorrowDays + "天之间");
        }

        // 2. 检查借阅数量限制
        long activeBorrows = borrowRecordRepository.countActiveBorrowsByUserId(user.getId());
        if (activeBorrows >= maxBorrowCount) {
            throw new BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED,
                    "您已借阅" + activeBorrows + "本书，最多可借" + maxBorrowCount + "本");
        }

        // 3. 检查图书是否存在
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // 4. 检查图书状态
        if (book.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BOOK_DISABLED);
        }

        // 5. 使用悲观锁检查是否已借阅该书（防止并发重复借阅）
        borrowRecordRepository.findActiveBorrowForUpdate(user.getId(), book.getId())
                .ifPresent(r -> {
                    throw new BusinessException(ErrorCode.ALREADY_BORROWED);
                });

        // 6. 使用原子操作减少库存（确保并发安全）
        int updated = bookRepository.decreaseAvailableCount(book.getId());
        if (updated == 0) {
            // 重新查询获取最新库存信息
            Book latestBook = bookRepository.findById(book.getId()).orElse(book);
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH,
                    "《" + latestBook.getTitle() + "》库存不足，当前可借数量为" + latestBook.getAvailableCount());
        }

        // 7. 创建借阅记录
        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(now)
                .dueDate(now.plusDays(days))
                .status(BorrowRecord.Status.BORROWING)
                .renewCount(0)
                .overdueDays(0)
                .fineAmount(BigDecimal.ZERO)
                .finePaid(false)
                .build();

        record = borrowRecordRepository.save(record);

        log.info("用户 {} 借阅图书《{}》成功，借阅天数: {} 天", user.getUsername(), book.getTitle(), days);

        // 8. 如果用户有该书的预约，标记为已完成
        try {
            reservationService.fulfillReservation(user.getId(), book.getId());
        } catch (Exception e) {
            log.warn("完成预约时出错（非关键）: {}", e.getMessage());
        }

        // 重新加载完整记录
        return BorrowRecordResponse.fromEntity(
                borrowRecordRepository.findByIdWithDetails(record.getId()).orElse(record),
                maxRenewCount
        );
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BorrowRecordResponse returnBook(Long recordId) {
        // 1. 校验记录ID
        if (recordId == null || recordId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "借阅记录ID无效");
        }

        // 2. 使用悲观锁查询借阅记录（防止并发归还）
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        // 3. 验证权限（本人或管理员）
        User currentUser = userService.getCurrentUserEntity();
        if (!record.getUser().getId().equals(currentUser.getId()) &&
            currentUser.getRole() != User.Role.ADMIN) {
            throw new BusinessException(ErrorCode.RETURN_PERMISSION_DENIED);
        }

        // 4. 检查状态
        if (record.getStatus() == BorrowRecord.Status.RETURNED) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED);
        }

        // 5. 计算逾期罚款
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(record.getDueDate())) {
            int overdueDays = record.calculateOverdueDays();
            // 使用可配置的罚款规则计算罚款
            BigDecimal fineAmount = fineService.calculateFine(overdueDays);
            record.setOverdueDays(overdueDays);
            record.setFineAmount(fineAmount);

            // 创建罚款记录（如果罚款金额大于0）
            if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                createFineRecordIfNotExists(record, overdueDays, fineAmount);
            }

            log.info("图书逾期 {} 天，罚款 {} 元", overdueDays, fineAmount);
        }

        // 6. 更新借阅记录
        record.setReturnDate(now);
        record.setStatus(BorrowRecord.Status.RETURNED);
        borrowRecordRepository.save(record);

        // 7. 使用原子操作增加库存
        int updated = bookRepository.increaseAvailableCount(record.getBook().getId());
        if (updated == 0) {
            log.warn("归还图书时库存增加失败，可能已达到最大值: bookId={}", record.getBook().getId());
        }

        log.info("用户 {} 归还图书《{}》成功", currentUser.getUsername(), record.getBook().getTitle());

        // 8. 通知下一位预约用户
        try {
            reservationService.notifyNextReservation(record.getBook().getId());
        } catch (Exception e) {
            log.warn("通知预约用户时出错（非关键）: {}", e.getMessage());
        }

        return BorrowRecordResponse.fromEntity(record, maxRenewCount);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BorrowRecordResponse renewBook(Long recordId) {
        // 1. 校验记录ID
        if (recordId == null || recordId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "借阅记录ID无效");
        }

        // 2. 使用悲观锁查询借阅记录
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        // 3. 验证权限（只能本人续借）
        User currentUser = userService.getCurrentUserEntity();
        if (!record.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "只能续借本人的借阅记录");
        }

        // 4. 检查是否可以续借
        if (record.getStatus() == BorrowRecord.Status.RETURNED) {
            throw new BusinessException(ErrorCode.RENEW_NOT_ALLOWED, "已归还的图书不能续借");
        }

        if (record.getStatus() == BorrowRecord.Status.OVERDUE || record.isOverdue()) {
            throw new BusinessException(ErrorCode.RENEW_OVERDUE_NOT_ALLOWED);
        }

        if (record.getRenewCount() >= maxRenewCount) {
            throw new BusinessException(ErrorCode.RENEW_LIMIT_EXCEEDED,
                    "已续借" + record.getRenewCount() + "次，最多可续借" + maxRenewCount + "次");
        }

        // 5. 执行续借：延长还书日期
        record.setDueDate(record.getDueDate().plusDays(renewDays));
        record.setRenewCount(record.getRenewCount() + 1);
        borrowRecordRepository.save(record);

        log.info("用户 {} 续借图书《{}》成功，续借次数: {}/{}，新到期日: {}",
                currentUser.getUsername(), record.getBook().getTitle(),
                record.getRenewCount(), maxRenewCount, record.getDueDate());

        return BorrowRecordResponse.fromEntity(record, maxRenewCount);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BorrowRecordResponse payFine(Long recordId) {
        // 1. 校验记录ID
        if (recordId == null || recordId <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "借阅记录ID无效");
        }

        // 2. 查询借阅记录
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        // 3. 验证权限（本人或管理员）
        User currentUser = userService.getCurrentUserEntity();
        if (!record.getUser().getId().equals(currentUser.getId()) &&
            currentUser.getRole() != User.Role.ADMIN) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作此记录");
        }

        // 4. 检查是否有罚款
        if (record.getFineAmount() == null || record.getFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该记录无需缴纳罚款");
        }

        // 5. 检查是否已缴纳
        if (Boolean.TRUE.equals(record.getFinePaid())) {
            throw new BusinessException(ErrorCode.FINE_ALREADY_PAID);
        }

        // 6. 标记罚款已缴纳
        record.setFinePaid(true);
        borrowRecordRepository.save(record);

        log.info("用户 {} 缴纳罚款 {} 元成功，借阅记录ID: {}",
                record.getUser().getUsername(), record.getFineAmount(), recordId);

        return BorrowRecordResponse.fromEntity(record, maxRenewCount);
    }

    @Override
    public BorrowRecordResponse getRecordById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "借阅记录ID无效");
        }

        BorrowRecord record = borrowRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        return BorrowRecordResponse.fromEntity(record, maxRenewCount);
    }

    @Override
    public PageResult<BorrowRecordResponse> getRecords(BorrowQueryRequest request) {
        // 校验分页参数
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int size = request.getSize() != null && request.getSize() > 0 ? Math.min(request.getSize(), 100) : 10;

        PageRequest pageRequest = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "borrowDate")
        );

        BorrowRecord.Status status = null;
        if (request.getStatus() != null) {
            if (request.getStatus() < 0 || request.getStatus() >= BorrowRecord.Status.values().length) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的借阅状态");
            }
            status = BorrowRecord.Status.values()[request.getStatus()];
        }

        Page<BorrowRecord> recordPage = borrowRecordRepository.findByConditions(
                request.getUserId(),
                request.getBookId(),
                status,
                pageRequest
        );

        return PageResult.of(
                recordPage.getContent().stream()
                        .map(record -> BorrowRecordResponse.fromEntity(record, maxRenewCount))
                        .collect(Collectors.toList()),
                recordPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageResult<BorrowRecordResponse> getMyRecords(int page, int size) {
        // 校验分页参数
        page = page > 0 ? page : 1;
        size = size > 0 ? Math.min(size, 100) : 10;

        User user = userService.getCurrentUserEntity();
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<BorrowRecord> recordPage = borrowRecordRepository.findByUserId(user.getId(), pageRequest);

        return PageResult.of(
                recordPage.getContent().stream()
                        .map(record -> BorrowRecordResponse.fromEntity(record, maxRenewCount))
                        .collect(Collectors.toList()),
                recordPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    public void checkOverdueRecords() {
        log.info("开始检查逾期借阅记录...");
        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(now);

        int count = 0;
        for (BorrowRecord record : overdueRecords) {
            // 更新状态为逾期
            record.setStatus(BorrowRecord.Status.OVERDUE);

            // 使用可配置的罚款规则计算罚款
            int overdueDays = record.calculateOverdueDays();
            BigDecimal fineAmount = fineService.calculateFine(overdueDays);
            record.setOverdueDays(overdueDays);
            record.setFineAmount(fineAmount);

            borrowRecordRepository.save(record);

            // 创建或更新罚款记录
            if (fineAmount.compareTo(BigDecimal.ZERO) > 0) {
                createOrUpdateFineRecord(record, overdueDays, fineAmount);
            }

            count++;

            log.debug("借阅记录 {} 已逾期 {} 天，罚款 {} 元",
                    record.getId(), overdueDays, fineAmount);
        }

        log.info("逾期检查完成，共标记 {} 条逾期记录", count);
    }

    @Override
    public List<BorrowRecordResponse> getAllRecordsForExport(BorrowQueryRequest request) {
        BorrowRecord.Status status = null;
        if (request.getStatus() != null) {
            if (request.getStatus() >= 0 && request.getStatus() < BorrowRecord.Status.values().length) {
                status = BorrowRecord.Status.values()[request.getStatus()];
            }
        }

        List<BorrowRecord> records = borrowRecordRepository.findAllByConditions(
                request.getUserId(),
                request.getBookId(),
                status
        );

        return records.stream()
                .map(record -> BorrowRecordResponse.fromEntity(record, maxRenewCount))
                .collect(Collectors.toList());
    }

    /**
     * 创建罚款记录（如果不存在）
     */
    private void createFineRecordIfNotExists(BorrowRecord borrowRecord, int overdueDays, BigDecimal amount) {
        // 检查是否已存在该借阅的罚款记录
        if (fineRecordRepository.findByBorrowRecordId(borrowRecord.getId()).isEmpty()) {
            FineRecord fineRecord = FineRecord.builder()
                    .user(borrowRecord.getUser())
                    .borrowRecord(borrowRecord)
                    .amount(amount)
                    .overdueDays(overdueDays)
                    .status(FineRecord.Status.UNPAID)
                    .build();
            fineRecordRepository.save(fineRecord);
            log.info("创建罚款记录: 用户={}, 借阅ID={}, 金额={}元",
                    borrowRecord.getUser().getUsername(), borrowRecord.getId(), amount);
        }
    }

    /**
     * 创建或更新罚款记录
     */
    private void createOrUpdateFineRecord(BorrowRecord borrowRecord, int overdueDays, BigDecimal amount) {
        fineRecordRepository.findByBorrowRecordId(borrowRecord.getId())
                .ifPresentOrElse(
                        record -> {
                            // 如果状态是未缴，更新金额和逾期天数
                            if (record.getStatus() == FineRecord.Status.UNPAID) {
                                record.setAmount(amount);
                                record.setOverdueDays(overdueDays);
                                fineRecordRepository.save(record);
                                log.debug("更新罚款记录: ID={}, 新金额={}元", record.getId(), amount);
                            }
                        },
                        () -> {
                            // 创建新记录
                            FineRecord fineRecord = FineRecord.builder()
                                    .user(borrowRecord.getUser())
                                    .borrowRecord(borrowRecord)
                                    .amount(amount)
                                    .overdueDays(overdueDays)
                                    .status(FineRecord.Status.UNPAID)
                                    .build();
                            fineRecordRepository.save(fineRecord);
                            log.info("创建罚款记录: 用户={}, 借阅ID={}, 金额={}元",
                                    borrowRecord.getUser().getUsername(), borrowRecord.getId(), amount);
                        }
                );
    }
}
