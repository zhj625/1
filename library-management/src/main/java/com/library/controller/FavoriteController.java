package com.library.controller;

import com.library.common.PageResult;
import com.library.common.Result;
import com.library.dto.response.FavoriteResponse;
import com.library.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{bookId}")
    public Result<FavoriteResponse> addFavorite(
            @PathVariable Long bookId,
            @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        return Result.success(favoriteService.addFavorite(bookId, remark));
    }

    @DeleteMapping("/{bookId}")
    public Result<Void> removeFavorite(@PathVariable Long bookId) {
        favoriteService.removeFavorite(bookId);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<FavoriteResponse>> getMyFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(favoriteService.getMyFavorites(page, size));
    }

    @GetMapping("/check/{bookId}")
    public Result<Boolean> checkFavorite(@PathVariable Long bookId) {
        return Result.success(favoriteService.isFavorited(bookId));
    }

    @GetMapping("/count/{bookId}")
    public Result<Long> getFavoriteCount(@PathVariable Long bookId) {
        return Result.success(favoriteService.getFavoriteCount(bookId));
    }
}
