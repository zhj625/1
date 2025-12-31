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
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${library.max-borrow-count:5}")
    private int maxBorrowCount;

    @Override
    @Transactional
    public BorrowRecordResponse borrowBook(BorrowRequest request) {
        User user = userService.getCurrentUserEntity();

        // 检查借阅数量限制
        long activeBorrows = borrowRecordRepository.countActiveBorrowsByUserId(user.getId());
        if (activeBorrows >= maxBorrowCount) {
            throw new BusinessException(ErrorCode.BORROW_LIMIT_EXCEEDED,
                    "您已借阅" + activeBorrows + "本书，最多可借" + maxBorrowCount + "本");
        }

        // 检查图书
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        if (!book.isAvailable()) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        // 检查是否已借阅该书
        borrowRecordRepository.findActiveBorrow(user.getId(), book.getId())
                .ifPresent(r -> {
                    throw new BusinessException(ErrorCode.ALREADY_BORROWED);
                });

        // 减少可借数量
        book.setAvailableCount(book.getAvailableCount() - 1);
        bookRepository.save(book);

        // 创建借阅记录
        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = BorrowRecord.builder()
                .user(user)
                .book(book)
                .borrowDate(now)
                .dueDate(now.plusDays(request.getDays()))
                .status(BorrowRecord.Status.BORROWING)
                .build();

        record = borrowRecordRepository.save(record);
        return BorrowRecordResponse.fromEntity(record);
    }

    @Override
    @Transactional
    public BorrowRecordResponse returnBook(Long recordId) {
        BorrowRecord record = borrowRecordRepository.findByIdWithDetails(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));

        // 验证权限（本人或管理员）
        User currentUser = userService.getCurrentUserEntity();
        if (!record.getUser().getId().equals(currentUser.getId()) &&
            currentUser.getRole() != User.Role.ADMIN) {
            throw new BusinessException(ErrorCode.NO_PERMISSION);
        }

        if (record.getStatus() == BorrowRecord.Status.RETURNED) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED);
        }

        // 更新借阅记录
        record.setReturnDate(LocalDateTime.now());
        record.setStatus(BorrowRecord.Status.RETURNED);
        borrowRecordRepository.save(record);

        // 增加可借数量
        Book book = record.getBook();
        book.setAvailableCount(book.getAvailableCount() + 1);
        bookRepository.save(book);

        return BorrowRecordResponse.fromEntity(record);
    }

    @Override
    public BorrowRecordResponse getRecordById(Long id) {
        BorrowRecord record = borrowRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECORD_NOT_FOUND));
        return BorrowRecordResponse.fromEntity(record);
    }

    @Override
    public PageResult<BorrowRecordResponse> getRecords(BorrowQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "borrowDate")
        );

        BorrowRecord.Status status = null;
        if (request.getStatus() != null) {
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
                        .map(BorrowRecordResponse::fromEntity)
                        .collect(Collectors.toList()),
                recordPage.getTotalElements(),
                request.getPage(),
                request.getSize()
        );
    }

    @Override
    public PageResult<BorrowRecordResponse> getMyRecords(int page, int size) {
        User user = userService.getCurrentUserEntity();
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<BorrowRecord> recordPage = borrowRecordRepository.findByUserId(user.getId(), pageRequest);

        return PageResult.of(
                recordPage.getContent().stream()
                        .map(BorrowRecordResponse::fromEntity)
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
        log.info("Checking overdue records...");
        List<BorrowRecord> overdueRecords = borrowRecordRepository.findOverdueRecords(LocalDateTime.now());

        for (BorrowRecord record : overdueRecords) {
            record.setStatus(BorrowRecord.Status.OVERDUE);
            borrowRecordRepository.save(record);
        }

        log.info("Found {} overdue records", overdueRecords.size());
    }

    @Override
    public List<BorrowRecordResponse> getAllRecordsForExport(BorrowQueryRequest request) {
        BorrowRecord.Status status = null;
        if (request.getStatus() != null) {
            status = BorrowRecord.Status.values()[request.getStatus()];
        }

        List<BorrowRecord> records = borrowRecordRepository.findAllByConditions(
                request.getUserId(),
                request.getBookId(),
                status
        );

        return records.stream()
                .map(BorrowRecordResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
