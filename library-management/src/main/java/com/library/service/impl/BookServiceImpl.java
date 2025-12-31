package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.common.PageResult;
import com.library.dto.request.BookQueryRequest;
import com.library.dto.request.BookRequest;
import com.library.dto.response.BookResponse;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.exception.BusinessException;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BookResponse createBook(BookRequest request) {
        if (StringUtils.hasText(request.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(ErrorCode.ISBN_EXISTS);
        }

        Book book = Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .publisher(request.getPublisher())
                .publishDate(request.getPublishDate())
                .price(request.getPrice())
                .totalCount(request.getTotalCount())
                .availableCount(request.getTotalCount())
                .description(request.getDescription())
                .coverUrl(request.getCoverUrl())
                .location(request.getLocation())
                .status(1)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            book.setCategory(category);
        }

        book = bookRepository.save(book);
        return BookResponse.fromEntity(book);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // 检查ISBN是否被其他图书使用
        if (StringUtils.hasText(request.getIsbn()) && !request.getIsbn().equals(book.getIsbn())) {
            if (bookRepository.existsByIsbn(request.getIsbn())) {
                throw new BusinessException(ErrorCode.ISBN_EXISTS);
            }
            book.setIsbn(request.getIsbn());
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishDate());
        book.setPrice(request.getPrice());

        // 更新库存
        int oldTotal = book.getTotalCount();
        int newTotal = request.getTotalCount();
        int diff = newTotal - oldTotal;
        book.setTotalCount(newTotal);
        book.setAvailableCount(Math.max(0, book.getAvailableCount() + diff));

        book.setDescription(request.getDescription());
        book.setCoverUrl(request.getCoverUrl());
        book.setLocation(request.getLocation());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }

        book = bookRepository.save(book);
        return BookResponse.fromEntity(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        return BookResponse.fromEntity(book);
    }

    @Override
    public PageResult<BookResponse> getBooks(BookQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Book> bookPage = bookRepository.findByConditions(
                request.getKeyword(),
                request.getCategoryId(),
                request.getStatus(),
                pageRequest
        );

        return PageResult.of(
                bookPage.getContent().stream()
                        .map(BookResponse::fromEntity)
                        .collect(Collectors.toList()),
                bookPage.getTotalElements(),
                request.getPage(),
                request.getSize()
        );
    }
}
