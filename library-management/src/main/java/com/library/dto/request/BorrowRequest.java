package com.library.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BorrowRequest {

    @NotNull(message = "图书ID不能为空")
    @Positive(message = "图书ID必须为正数")
    private Long bookId;

    @Min(value = 1, message = "借阅天数最少为1天")
    @Max(value = 90, message = "借阅天数最多为90天")
    private Integer days = 30;  // 借阅天数，默认30天
}
