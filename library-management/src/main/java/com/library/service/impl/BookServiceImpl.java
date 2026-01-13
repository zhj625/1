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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BookResponse createBook(BookRequest request) {
        // 1. 校验ISBN唯一性
        if (StringUtils.hasText(request.getIsbn())) {
            String isbn = request.getIsbn().trim();
            if (bookRepository.existsByIsbn(isbn)) {
                throw new BusinessException(ErrorCode.ISBN_EXISTS, "ISBN \"" + isbn + "\" 已被使用");
            }
        }

        // 2. 校验分类
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        // 3. 创建图书
        Book book = Book.builder()
                .isbn(StringUtils.hasText(request.getIsbn()) ? request.getIsbn().trim() : null)
                .title(request.getTitle().trim())
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
                .category(category)
                .build();

        book = bookRepository.save(book);
        log.info("创建图书成功: id={}, title={}", book.getId(), book.getTitle());
        return BookResponse.fromEntity(book);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        // 1. 校验ID
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图书ID无效");
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // 2. 检查ISBN是否被其他图书使用
        if (StringUtils.hasText(request.getIsbn())) {
            String newIsbn = request.getIsbn().trim();
            if (!newIsbn.equals(book.getIsbn()) && bookRepository.existsByIsbn(newIsbn)) {
                throw new BusinessException(ErrorCode.ISBN_EXISTS, "ISBN \"" + newIsbn + "\" 已被使用");
            }
            book.setIsbn(newIsbn);
        } else {
            book.setIsbn(null);
        }

        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishDate());
        book.setPrice(request.getPrice());

        // 3. 更新库存（确保可借数量不超过总库存且不为负）
        int oldTotal = book.getTotalCount();
        int newTotal = request.getTotalCount();
        int diff = newTotal - oldTotal;

        // 计算新的可借数量
        int newAvailable = book.getAvailableCount() + diff;
        if (newAvailable < 0) {
            // 如果新的可借数量为负，说明减少的库存超过了当前可借数量
            long borrowedCount = oldTotal - book.getAvailableCount();
            throw new BusinessException(ErrorCode.STOCK_INVALID,
                    "无法减少库存：当前有" + borrowedCount + "本书被借出，总库存不能少于" + borrowedCount);
        }
        if (newAvailable > newTotal) {
            newAvailable = newTotal;
        }

        book.setTotalCount(newTotal);
        book.setAvailableCount(newAvailable);

        book.setDescription(request.getDescription());
        book.setCoverUrl(request.getCoverUrl());
        book.setLocation(request.getLocation());

        // 4. 更新分类
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }

        book = bookRepository.save(book);
        log.info("更新图书成功: id={}, title={}", book.getId(), book.getTitle());
        return BookResponse.fromEntity(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        // 1. 校验ID
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图书ID无效");
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // 2. 检查是否有未归还的借阅记录
        long activeBorrows = bookRepository.countActiveBorrowsByBookId(id);
        if (activeBorrows > 0) {
            throw new BusinessException(ErrorCode.BOOK_HAS_ACTIVE_BORROWS,
                    "该图书有" + activeBorrows + "条未归还的借阅记录，无法删除");
        }

        bookRepository.deleteById(id);
        log.info("删除图书成功: id={}, title={}", id, book.getTitle());
    }

    @Override
    public BookResponse getBookById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图书ID无效");
        }

        Book book = bookRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        return BookResponse.fromEntity(book);
    }

    @Override
    public PageResult<BookResponse> getBooks(BookQueryRequest request) {
        // 校验分页参数
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int size = request.getSize() != null && request.getSize() > 0 ? Math.min(request.getSize(), 100) : 10;

        PageRequest pageRequest = PageRequest.of(
                page - 1,
                size,
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
                page,
                size
        );
    }

    @Override
    public List<BookResponse> getNewArrivals(int days, int limit) {
        // 校验参数
        days = days > 0 ? days : 30;
        limit = limit > 0 ? Math.min(limit, 50) : 10;

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        PageRequest pageRequest = PageRequest.of(0, limit);

        List<Book> books = bookRepository.findNewArrivals(since, pageRequest);
        log.debug("获取新书推荐，天数: {}, 限制: {}, 结果数量: {}", days, limit, books.size());

        return books.stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getPopularBooks(int limit) {
        limit = limit > 0 ? Math.min(limit, 50) : 10;

        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findPopularBooks(pageRequest);
        log.debug("获取热门图书，限制: {}, 结果数量: {}", limit, books.size());

        return books.stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
