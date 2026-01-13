package com.library.service;

import com.library.common.PageResult;
import com.library.dto.request.FineRuleRequest;
import com.library.dto.response.FineRecordResponse;
import com.library.dto.response.FineRuleResponse;
import com.library.entity.FineRecord;
import com.library.entity.FineRule;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 罚款服务接口
 */
public interface FineService {

    // ========== 罚款规则相关 ==========

    /**
     * 获取当前罚款规则
     */
    FineRuleResponse getCurrentRule();

    /**
     * 获取当前有效的罚款规则实体（供内部使用）
     */
    FineRule getActiveRuleEntity();

    /**
     * 更新罚款规则
     */
    FineRuleResponse updateRule(FineRuleRequest request);

    // ========== 罚款记录相关 ==========

    /**
     * 创建罚款记录
     */
    FineRecordResponse createFineRecord(Long borrowId, int overdueDays);

    /**
     * 查询所有罚款记录（管理端）
     */
    PageResult<FineRecordResponse> getFineRecords(Long userId, String status, String username, int page, int size);

    /**
     * 查询当前用户的罚款记录
     */
    PageResult<FineRecordResponse> getMyFineRecords(String status, int page, int size);

    /**
     * 根据ID查询罚款记录
     */
    FineRecordResponse getFineRecordById(Long id);

    /**
     * 缴纳罚款
     */
    FineRecordResponse payFine(Long id);

    /**
     * 免除罚款（管理员操作）
     */
    FineRecordResponse waiveFine(Long id, String reason);

    /**
     * 获取当前用户未缴罚款总额
     */
    BigDecimal getMyUnpaidAmount();

    /**
     * 获取指定用户未缴罚款总额
     */
    BigDecimal getUnpaidAmountByUserId(Long userId);

    /**
     * 获取罚款统计信息
     */
    Map<String, Object> getFineStatistics();

    /**
     * 计算逾期罚款金额（使用当前规则）
     */
    BigDecimal calculateFine(int overdueDays);
}
