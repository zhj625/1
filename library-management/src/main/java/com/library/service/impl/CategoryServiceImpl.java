package com.library.service.impl;

import com.library.common.ErrorCode;
import com.library.dto.request.CategoryRequest;
import com.library.dto.response.CategoryResponse;
import com.library.entity.Category;
import com.library.exception.BusinessException;
import com.library.repository.CategoryRepository;
import com.library.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 最大分类层级深度
     */
    private static final int MAX_CATEGORY_DEPTH = 3;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // 1. 校验分类名称唯一性
        if (categoryRepository.existsByName(request.getName().trim())) {
            throw new BusinessException(ErrorCode.CATEGORY_EXISTS);
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        // 2. 处理父分类
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_PARENT_NOT_FOUND));

            // 检查层级深度
            int depth = getCategoryDepth(parent) + 1;
            if (depth > MAX_CATEGORY_DEPTH) {
                throw new BusinessException(ErrorCode.CATEGORY_DEPTH_EXCEEDED,
                        "分类层级不能超过" + MAX_CATEGORY_DEPTH + "层");
            }

            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        log.info("创建分类成功: id={}, name={}", category.getId(), category.getName());
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        // 1. 校验ID
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类ID无效");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. 检查名称是否被其他分类使用
        String newName = request.getName().trim();
        if (!category.getName().equals(newName) && categoryRepository.existsByName(newName)) {
            throw new BusinessException(ErrorCode.CATEGORY_EXISTS);
        }

        category.setName(newName);
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        // 3. 处理父分类变更
        if (request.getParentId() != null) {
            // 不能将自己设为父分类
            if (request.getParentId().equals(id)) {
                throw new BusinessException(ErrorCode.CATEGORY_PARENT_INVALID, "不能将自己设为父分类");
            }

            // 不能将自己的子分类设为父分类（防止循环引用）
            if (isDescendant(id, request.getParentId())) {
                throw new BusinessException(ErrorCode.CATEGORY_PARENT_INVALID, "不能将子分类设为父分类");
            }

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_PARENT_NOT_FOUND));

            // 检查层级深度（当前分类的最大子树深度 + 新父分类深度）
            int maxSubtreeDepth = getMaxSubtreeDepth(category);
            int parentDepth = getCategoryDepth(parent);
            if (parentDepth + maxSubtreeDepth > MAX_CATEGORY_DEPTH) {
                throw new BusinessException(ErrorCode.CATEGORY_DEPTH_EXCEEDED,
                        "移动后分类层级将超过" + MAX_CATEGORY_DEPTH + "层");
            }

            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoryRepository.save(category);
        log.info("更新分类成功: id={}, name={}", category.getId(), category.getName());
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // 1. 校验ID
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类ID无效");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 2. 检查是否有子分类
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN,
                    "该分类下存在" + category.getChildren().size() + "个子分类，请先删除子分类");
        }

        // 3. 检查是否有关联的图书
        long bookCount = categoryRepository.countBooksByCategoryId(id);
        if (bookCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_BOOKS,
                    "该分类下存在" + bookCount + "本图书，无法删除");
        }

        categoryRepository.deleteById(id);
        log.info("删除分类成功: id={}, name={}", id, category.getName());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类ID无效");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        return CategoryResponse.fromEntity(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findRootCategories();
        return rootCategories.stream()
                .map(CategoryResponse::fromEntityWithChildren)
                .collect(Collectors.toList());
    }

    /**
     * 获取分类的层级深度（根分类为1）
     */
    private int getCategoryDepth(Category category) {
        int depth = 1;
        Category parent = category.getParent();
        while (parent != null) {
            depth++;
            parent = parent.getParent();
        }
        return depth;
    }

    /**
     * 获取分类子树的最大深度
     */
    private int getMaxSubtreeDepth(Category category) {
        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return 1;
        }
        int maxChildDepth = 0;
        for (Category child : category.getChildren()) {
            maxChildDepth = Math.max(maxChildDepth, getMaxSubtreeDepth(child));
        }
        return maxChildDepth + 1;
    }

    /**
     * 检查potentialDescendantId是否是ancestorId的后代
     */
    private boolean isDescendant(Long ancestorId, Long potentialDescendantId) {
        Set<Long> descendants = new HashSet<>();
        collectDescendantIds(ancestorId, descendants);
        return descendants.contains(potentialDescendantId);
    }

    /**
     * 收集所有后代分类的ID
     */
    private void collectDescendantIds(Long categoryId, Set<Long> descendants) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null && category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                descendants.add(child.getId());
                collectDescendantIds(child.getId(), descendants);
            }
        }
    }
}
