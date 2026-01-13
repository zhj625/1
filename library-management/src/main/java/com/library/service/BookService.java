package com.library.service;

import com.library.common.PageResult;
import com.library.dto.request.BookQueryRequest;
import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;

import java.util.List;

public interface BookService {

    BookResponse createBook(BookRequest request);

    BookResponse updateBook(Long id, BookRequest request);

    void deleteBook(Long id);

    BookResponse getBookById(Long id);

    PageResult<BookResponse> getBooks(BookQueryRequest request);

    /**
     * 获取新书推荐（最近N天内入库的图书）
     * @param days 天数，默认30天
     * @param limit 返回数量限制
     */
    List<BookResponse> getNewArrivals(int days, int limit);

    /**
     * 获取热门图书（按借阅次数排序）
     * @param limit 返回数量限制
     */
    List<BookResponse> getPopularBooks(int limit);
}
