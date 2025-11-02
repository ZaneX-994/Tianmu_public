package com.bytewizard.videoactionservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.videoactionservice.domain.entity.Category;
import com.bytewizard.videoactionservice.domain.vo.CategoryListResponse;
import com.bytewizard.videoactionservice.mapper.CategoryMapper;
import com.bytewizard.videoactionservice.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public List<CategoryListResponse> categoryList() {
        List<Category> categoryList = this.list();

        return categoryList.stream().map(category -> {
            CategoryListResponse categoryListResponse = new CategoryListResponse();
            categoryListResponse.setCategoryId(category.getCategoryId());
            categoryListResponse.setCategoryName(category.getCategoryName());
            return categoryListResponse;
        }).collect(Collectors.toList());

    }
}
