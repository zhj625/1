package com.library.dto.request;

import lombok.Data;

@Data
public class BookQueryRequest {

    private String keyword;      // 搜索关键字(书名/作者/ISBN)
    private Long categoryId;     // 分类ID
    private Integer status;      // 状态
    private Integer page = 1;    // 页码
    private Integer size = 10;   // 每页数量
}
