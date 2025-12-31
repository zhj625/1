package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.request.BookQueryRequest;
import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import com.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public Result<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return Result.success(bookService.createBook(request));
    }

    @PutMapping("/{id}")
    public Result<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        return Result.success(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<BookResponse> getBookById(@PathVariable Long id) {
        return Result.success(bookService.getBookById(id));
    }

    @GetMapping
    public Result<PageResult<BookResponse>> getBooks(BookQueryRequest request) {
        return Result.success(bookService.getBooks(request));
    }
}
