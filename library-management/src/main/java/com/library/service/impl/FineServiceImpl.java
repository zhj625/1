package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.common.PageResult;
import com.library.dto.request.FineRuleRequest;
import com.library.dto.response.FineRecordResponse;
import com.library.dto.response.FineRuleResponse;
import com.library.entity.BorrowRecord;
import com.library.entity.FineRecord;
import com.library.entity.FineRule;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.FineRecordRepository;
import com.library.repository.FineRuleRepository;
import com.library.service.FineService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 罚款服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {

    private final FineRuleRepository fineRuleRepository;
    private final FineRecordRepository fineRecordRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserService userService;

    // 默认罚款规则
    private static final BigDecimal DEFAULT_DAILY_AMOUNT = new BigDecimal("0.50");
    private static final BigDecimal DEFAULT_MAX_AMOUNT = new BigDecimal("100.00");
    private static final int DEFAULT_GRACE_DAYS = 0;

    // ========== 罚款规则相关 ==========

    @Override
    public FineRuleResponse getCurrentRule() {
        FineRule rule = getActiveRuleEntity();
        return FineRuleResponse.fromEntity(rule);
    }

    @Override
    public FineRule getActiveRuleEntity() {
        return fineRuleRepository.findActiveRule()
                .orElseGet(this::createDefaultRule);
    }

    @Override
    @Transactional
    public FineRuleResponse updateRule(FineRuleRequest request) {
        FineRule rule = fineRuleRepository.findLatestRule()
                .orElseGet(() -> FineRule.builder().enabled(true).build());

        rule.setDailyAmount(request.getDailyAmount());
        rule.setMaxAmount(request.getMaxAmount());
        rule.setGraceDays(request.getGraceDays());
        rule.setDescription(request.getDescription());
        rule.setEnabled(true);

        rule = fineRuleRepository.save(rule);
        log.info("罚款规则已更新: 每日罚金={}元, 封顶={}元, 免罚天数={}天",
                rule.getDailyAmount(), rule.getMaxAmount(), rule.getGraceDays());

        return FineRuleResponse.fromEntity(rule);
    }

    private FineRule createDefaultRule() {
        FineRule rule = FineRule.builder()
                .dailyAmount(DEFAULT_DAILY_AMOUNT)
                .maxAmount(DEFAULT_MAX_AMOUNT)
                .graceDays(DEFAULT_GRACE_DAYS)
                .description("默认罚款规则：每日0.5元，封顶100元")
                .enabled(true)
                .build();
        return fineRuleRepository.save(rule);
    }

    // ========== 罚款记录相关 ==========

    @Override
    @Transactional
    public FineRecordResponse createFineRecord(Long borrowId, int overdueDays) {
        // 检查是否已存在该借阅的罚款记录
        if (fineRecordRepository.findByBorrowRecordId(borrowId).isPresent()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该借阅记录已存在罚款记录");
        }

        BorrowRecord borrowRecord = borrowRecordRepository.findByIdWithDetails(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        BigDecimal amount = calculateFine(overdueDays);

        FineRecord record = FineRecord.builder()
                .user(borrowRecord.getUser())
                .borrowRecord(borrowRecord)
                .amount(amount)
                .overdueDays(overdueDays)
                .status(FineRecord.Status.UNPAID)
                .build();

        record = fineRecordRepository.save(record);
        log.info("创建罚款记录: 用户={}, 借阅ID={}, 逾期天数={}, 罚款金额={}元",
                borrowRecord.getUser().getUsername(), borrowId, overdueDays, amount);

        return FineRecordResponse.fromEntity(record);
    }

    @Override
    public PageResult<FineRecordResponse> getFineRecords(Long userId, String status, String username, int page, int size) {
        page = page > 0 ? page : 1;
        size = size > 0 ? Math.min(size, 100) : 10;

        FineRecord.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = FineRecord.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略无效状态
            }
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<FineRecord> recordPage = fineRecordRepository.findByConditions(userId, statusEnum, username, pageRequest);

        return PageResult.of(
                recordPage.getContent().stream()
                        .map(FineRecordResponse::fromEntity)
                        .collect(Collectors.toList()),
                recordPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public PageResult<FineRecordResponse> getMyFineRecords(String status, int page, int size) {
        User currentUser = userService.getCurrentUserEntity();
        page = page > 0 ? page : 1;
        size = size > 0 ? Math.min(size, 100) : 10;

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<FineRecord> recordPage;

        if (status != null && !status.isEmpty()) {
            try {
                FineRecord.Status statusEnum = FineRecord.Status.valueOf(status.toUpperCase());
                recordPage = fineRecordRepository.findByUserIdAndStatus(currentUser.getId(), statusEnum, pageRequest);
            } catch (IllegalArgumentException e) {
                recordPage = fineRecordRepository.findByUserId(currentUser.getId(), pageRequest);
            }
        } else {
            recordPage = fineRecordRepository.findByUserId(currentUser.getId(), pageRequest);
        }

        return PageResult.of(
                recordPage.getContent().stream()
                        .map(FineRecordResponse::fromEntity)
                        .collect(Collectors.toList()),
                recordPage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public FineRecordResponse getFineRecordById(Long id) {
        FineRecord record = fineRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "罚款记录不存在"));
        return FineRecordResponse.fromEntity(record);
    }

    @Override
    @Transactional
    public FineRecordResponse payFine(Long id) {
        FineRecord record = fineRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "罚款记录不存在"));

        // 验证权限（本人或管理员）
        User currentUser = userService.getCurrentUserEntity();
        if (!record.getUser().getId().equals(currentUser.getId())
                && currentUser.getRole() != User.Role.ADMIN) {
            throw new BusinessException(ErrorCode.NO_PERMISSION, "无权操作此罚款记录");
        }

        if (record.getStatus() != FineRecord.Status.UNPAID) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该罚款记录状态不是未缴，无法缴费");
        }

        record.markAsPaid();
        fineRecordRepository.save(record);

        // 同步更新借阅记录的罚款状态
        BorrowRecord borrowRecord = record.getBorrowRecord();
        if (borrowRecord != null) {
            borrowRecord.setFinePaid(true);
            borrowRecordRepository.save(borrowRecord);
        }

        log.info("用户 {} 缴纳罚款 {} 元成功，罚款记录ID: {}",
                record.getUser().getUsername(), record.getAmount(), id);

        return FineRecordResponse.fromEntity(record);
    }

    @Override
    @Transactional
    public FineRecordResponse waiveFine(Long id, String reason) {
        FineRecord record = fineRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR, "罚款记录不存在"));

        if (record.getStatus() != FineRecord.Status.UNPAID) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该罚款记录状态不是未缴，无法免除");
        }

        User currentUser = userService.getCurrentUserEntity();
        record.markAsWaived(reason, currentUser.getId(), currentUser.getUsername());
        fineRecordRepository.save(record);

        // 同步更新借阅记录的罚款状态
        BorrowRecord borrowRecord = record.getBorrowRecord();
        if (borrowRecord != null) {
            borrowRecord.setFinePaid(true); // 免除也标记为已处理
            borrowRecordRepository.save(borrowRecord);
        }

        log.info("管理员 {} 免除用户 {} 的罚款 {} 元，原因: {}",
                currentUser.getUsername(), record.getUser().getUsername(), record.getAmount(), reason);

        return FineRecordResponse.fromEntity(record);
    }

    @Override
    public BigDecimal getMyUnpaidAmount() {
        User currentUser = userService.getCurrentUserEntity();
        return fineRecordRepository.sumUnpaidAmountByUserId(currentUser.getId());
    }

    @Override
    public BigDecimal getUnpaidAmountByUserId(Long userId) {
        return fineRecordRepository.sumUnpaidAmountByUserId(userId);
    }

    @Override
    public Map<String, Object> getFineStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总未缴金额
        stats.put("totalUnpaidAmount", fineRecordRepository.sumAllUnpaidAmount());

        // 各状态数量
        List<Object[]> statusCounts = fineRecordRepository.countByStatus();
        Map<String, Long> statusCountMap = new HashMap<>();
        long totalCount = 0;
        for (Object[] row : statusCounts) {
            FineRecord.Status status = (FineRecord.Status) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status.name(), count);
            totalCount += count;
        }
        stats.put("statusCounts", statusCountMap);
        stats.put("totalCount", totalCount);

        // 当前规则
        FineRule rule = getActiveRuleEntity();
        stats.put("currentRule", FineRuleResponse.fromEntity(rule));

        return stats;
    }

    @Override
    public BigDecimal calculateFine(int overdueDays) {
        FineRule rule = getActiveRuleEntity();
        return rule.calculateFine(overdueDays);
    }
}
