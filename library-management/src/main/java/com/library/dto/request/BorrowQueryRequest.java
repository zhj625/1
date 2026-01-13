package com.library.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "借阅记录查询条件")
public class BorrowQueryRequest {

    @Parameter(description = "用户ID", example = "1")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Parameter(description = "图书ID", example = "1")
    @Schema(description = "图书ID", example = "1")
    private Long bookId;

    @Parameter(description = "状态：0-借阅中，1-已归还，2-逾期", example = "0")
    @Schema(description = "状态：0-借阅中，1-已归还，2-逾期", example = "0")
    private Integer status;

    @Parameter(description = "页码，从1开始", example = "1")
    @Schema(description = "页码，从1开始", example = "1", defaultValue = "1")
    private Integer page = 1;

    @Parameter(description = "每页数量", example = "10")
    @Schema(description = "每页数量", example = "10", defaultValue = "10")
    private Integer size = 10;
}
