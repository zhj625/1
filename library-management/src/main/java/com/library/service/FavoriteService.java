package com.library.service;

import com.library.common.PageResult;
import com.library.dto.response.FavoriteResponse;

public interface FavoriteService {

    FavoriteResponse addFavorite(Long bookId, String remark);

    void removeFavorite(Long bookId);

    PageResult<FavoriteResponse> getMyFavorites(int page, int size);

    boolean isFavorited(Long bookId);

    long getFavoriteCount(Long bookId);
}
