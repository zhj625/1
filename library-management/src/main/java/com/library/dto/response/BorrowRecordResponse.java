package com.library.dto.response;

import com.library.entity.BorrowRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static BorrowRecordResponse fromEntity(BorrowRecord record) {
        String statusDesc;
        switch (record.getStatus()) {
            case BORROWING -> statusDesc = "借阅中";
            case RETURNED -> statusDesc = "已归还";
            case OVERDUE -> statusDesc = "逾期";
            default -> statusDesc = "未知";
        }

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
                .build();
    }
}
