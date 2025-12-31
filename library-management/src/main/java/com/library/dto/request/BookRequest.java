package com.library.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookRequest {

    @Size(max = 20, message = "ISBN不超过20个字符")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名不超过200个字符")
    private String title;

    @Size(max = 100, message = "作者不超过100个字符")
    private String author;

    @Size(max = 100, message = "出版社不超过100个字符")
    private String publisher;

    private LocalDate publishDate;

    private Long categoryId;

    private BigDecimal price;

    @NotNull(message = "总库存不能为空")
    @Min(value = 1, message = "总库存至少为1")
    private Integer totalCount;

    private String description;

    private String coverUrl;

    @Size(max = 50, message = "存放位置不超过50个字符")
    private String location;
}
