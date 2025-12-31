package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.common.PageResult;
import com.library.dto.response.FavoriteResponse;
import com.library.entity.Book;
import com.library.entity.Favorite;
import com.library.entity.User;
import com.library.exception.BusinessException;
import com.library.repository.BookRepository;
import com.library.repository.FavoriteRepository;
import com.library.service.FavoriteService;
import com.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final BookRepository bookRepository;
    private final UserService userService;

    @Override
    @Transactional
    public FavoriteResponse addFavorite(Long bookId, String remark) {
        User user = userService.getCurrentUserEntity();

        // 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "已经收藏过该图书");
        }

        // 检查图书是否存在
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        Favorite favorite = Favorite.builder()
                .user(user)
                .book(book)
                .remark(remark)
                .build();

        favorite = favoriteRepository.save(favorite);
        return FavoriteResponse.fromEntity(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long bookId) {
        User user = userService.getCurrentUserEntity();
        favoriteRepository.deleteByUserIdAndBookId(user.getId(), bookId);
    }

    @Override
    public PageResult<FavoriteResponse> getMyFavorites(int page, int size) {
        User user = userService.getCurrentUserEntity();
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> favoritePage = favoriteRepository.findByUserIdWithBook(user.getId(), pageRequest);

        return PageResult.of(
                favoritePage.getContent().stream()
                        .map(FavoriteResponse::fromEntity)
                        .collect(Collectors.toList()),
                favoritePage.getTotalElements(),
                page,
                size
        );
    }

    @Override
    public boolean isFavorited(Long bookId) {
        User user = userService.getCurrentUserEntity();
        return favoriteRepository.existsByUserIdAndBookId(user.getId(), bookId);
    }

    @Override
    public long getFavoriteCount(Long bookId) {
        return favoriteRepository.countByBookId(bookId);
    }
}
