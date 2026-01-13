package com.library.dto.response;

import com.library.entity.FineRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 罚款记录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineRecordResponse {

    /**
     * 罚款记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 借阅记录ID
     */
    private Long borrowId;

    /**
     * 图书ID
     */
    private Long bookId;

    /**
     * 图书标题
     */
    private String bookTitle;

    /**
     * 罚款金额
     */
    private BigDecimal amount;

    /**
     * 逾期天数
     */
    private Integer overdueDays;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 缴费时间
     */
    private LocalDateTime paidAt;

    /**
     * 免除时间
     */
    private LocalDateTime waivedAt;

    /**
     * 免除原因
     */
    private String waiveReason;

    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 从实体转换
     */
    public static FineRecordResponse fromEntity(FineRecord record) {
        if (record == null) {
            return null;
        }
        return FineRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUser() != null ? record.getUser().getId() : null)
                .username(record.getUser() != null ? record.getUser().getUsername() : null)
                .realName(record.getUser() != null ? record.getUser().getRealName() : null)
                .borrowId(record.getBorrowRecord() != null ? record.getBorrowRecord().getId() : null)
                .bookId(record.getBorrowRecord() != null && record.getBorrowRecord().getBook() != null
                        ? record.getBorrowRecord().getBook().getId() : null)
                .bookTitle(record.getBorrowRecord() != null && record.getBorrowRecord().getBook() != null
                        ? record.getBorrowRecord().getBook().getTitle() : null)
                .amount(record.getAmount())
                .overdueDays(record.getOverdueDays())
                .status(record.getStatus().name())
                .statusText(record.getStatus().getDescription())
                .paidAt(record.getPaidAt())
                .waivedAt(record.getWaivedAt())
                .waiveReason(record.getWaiveReason())
                .operatorName(record.getOperatorName())
                .remark(record.getRemark())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
