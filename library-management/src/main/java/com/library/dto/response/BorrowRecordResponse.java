package com.library.dto.response;

import com.library.entity.BorrowRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordResponse {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private String bookAuthor;
    private String bookCoverUrl;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Integer status;
    private String statusDesc;
    private String remark;
    private Boolean overdue;
    private LocalDateTime createdAt;

    // 续借相关
    private Integer renewCount;
    private Boolean canRenew;
    private Integer maxRenewCount;  // 最大续借次数（来自配置）

    // 罚款相关
    private Integer overdueDays;
    private BigDecimal fineAmount;
    private Boolean finePaid;

    /**
     * 从实体转换（使用默认续借上限 2）
     */
    public static BorrowRecordResponse fromEntity(BorrowRecord record) {
        return fromEntity(record, 2);  // 默认值与配置文件默认值保持一致
    }

    /**
     * 从实体转换（使用指定的续借上限）
     *
     * @param record       借阅记录实体
     * @param maxRenewCount 最大续借次数（来自配置）
     */
    public static BorrowRecordResponse fromEntity(BorrowRecord record, int maxRenewCount) {
        String statusDesc;
        switch (record.getStatus()) {
            case BORROWING -> statusDesc = "借阅中";
            case RETURNED -> statusDesc = "已归还";
            case OVERDUE -> statusDesc = "逾期";
            default -> statusDesc = "未知";
        }

        // 判断是否可以续借（未归还、未逾期、续借次数未达上限）
        boolean canRenew = record.getStatus() == BorrowRecord.Status.BORROWING
                && !record.isOverdue()
                && record.getRenewCount() < maxRenewCount;

        // 获取逾期天数：已归还记录使用存储值，未归还记录实时计算
        int overdueDays;
        if (record.getStatus() == BorrowRecord.Status.RETURNED) {
            // 已归还：使用归还时保存的逾期天数
            overdueDays = record.getOverdueDays() != null ? record.getOverdueDays() : 0;
        } else {
            // 未归还：实时计算当前逾期天数
            overdueDays = record.calculateOverdueDays();
        }

        // 获取罚款金额：优先使用存储值，未存储则实时计算
        BigDecimal fineAmount = (record.getFineAmount() != null && record.getFineAmount().compareTo(BigDecimal.ZERO) > 0)
                ? record.getFineAmount()
                : record.calculateFine();

        return BorrowRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUser().getId())
                .username(record.getUser().getUsername())
                .realName(record.getUser().getRealName())
                .bookId(record.getBook().getId())
                .bookTitle(record.getBook().getTitle())
                .bookIsbn(record.getBook().getIsbn())
                .bookAuthor(record.getBook().getAuthor())
                .bookCoverUrl(record.getBook().getCoverUrl())
                .borrowDate(record.getBorrowDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus().getValue())
                .statusDesc(statusDesc)
                .remark(record.getRemark())
                .overdue(record.isOverdue())
                .createdAt(record.getCreatedAt())
                .renewCount(record.getRenewCount())
                .canRenew(canRenew)
                .maxRenewCount(maxRenewCount)
                .overdueDays(overdueDays)
                .fineAmount(fineAmount)
                .finePaid(record.getFinePaid())
                .build();
    }
}
