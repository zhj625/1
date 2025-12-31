package com.library.service;

import com.library.common.PageResult;
import com.library.dto.request.BookQueryRequest;
import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;

public interface BookService {

    BookResponse createBook(BookRequest request);

    BookResponse updateBook(Long id, BookRequest request);

    void deleteBook(Long id);

    BookResponse getBookById(Long id);

    PageResult<BookResponse> getBooks(BookQueryRequest request);
}
