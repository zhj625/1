package com.library.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookRequest {

    @Pattern(regexp = "^$|^[0-9\\-]{10,20}$", message = "ISBN格式不正确")
    @Size(max = 20, message = "ISBN不超过20个字符")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    @Size(min = 1, max = 200, message = "书名长度为1-200个字符")
    private String title;

    @Size(max = 100, message = "作者不超过100个字符")
    private String author;

    @Size(max = 100, message = "出版社不超过100个字符")
    private String publisher;

    @PastOrPresent(message = "出版日期不能是未来日期")
    private LocalDate publishDate;

    @Positive(message = "分类ID必须为正数")
    private Long categoryId;

    @DecimalMin(value = "0.00", message = "价格不能为负数")
    @DecimalMax(value = "99999.99", message = "价格不能超过99999.99")
    private BigDecimal price;

    @NotNull(message = "总库存不能为空")
    @Min(value = 0, message = "总库存不能为负数")
    @Max(value = 9999, message = "总库存不能超过9999")
    private Integer totalCount;

    @Size(max = 2000, message = "描述不超过2000个字符")
    private String description;

    @Size(max = 500, message = "封面URL不超过500个字符")
    private String coverUrl;

    @Size(max = 50, message = "存放位置不超过50个字符")
    private String location;
}
