package com.library.dto.request;

import lombok.Data;

@Data
public class BorrowQueryRequest {

    private Long userId;         // 用户ID
    private Long bookId;         // 图书ID
    private Integer status;      // 状态: 0-借阅中, 1-已归还, 2-逾期
    private Integer page = 1;    // 页码
    private Integer size = 10;   // 每页数量
}
