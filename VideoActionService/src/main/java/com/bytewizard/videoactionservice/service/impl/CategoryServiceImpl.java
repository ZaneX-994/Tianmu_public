package com.bytewizard.videoactionservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.videoactionservice.domain.entity.Category;
import com.bytewizard.videoactionservice.domain.vo.CategoryListResponse;
import com.bytewizard.videoactionservice.mapper.CategoryMapper;
import com.bytewizard.videoactionservice.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public List<CategoryListResponse> categoryList() {
        return null;
    }
}
