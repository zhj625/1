package com.library.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "图书查询条件")
public class BookQueryRequest {

    @Parameter(description = "搜索关键字（书名/作者/ISBN）", example = "Java")
    @Schema(description = "搜索关键字（书名/作者/ISBN）", example = "Java")
    private String keyword;

    @Parameter(description = "分类ID", example = "1")
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Parameter(description = "状态：1-上架，0-下架", example = "1")
    @Schema(description = "状态：1-上架，0-下架", example = "1")
    private Integer status;

    @Parameter(description = "页码，从1开始", example = "1")
    @Schema(description = "页码，从1开始", example = "1", defaultValue = "1")
    private Integer page = 1;

    @Parameter(description = "每页数量", example = "10")
    @Schema(description = "每页数量", example = "10", defaultValue = "10")
    private Integer size = 10;
}
