package com.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {

    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    private Integer days = 30;  // 借阅天数，默认30天
}
