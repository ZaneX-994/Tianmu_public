package com.bytewizard.videoactionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.videoactionservice.domain.entity.Category;
import com.bytewizard.videoactionservice.domain.vo.CategoryListResponse;

import java.util.List;

public interface CategoryService extends IService<Category> {

    List<CategoryListResponse> categoryList();

}

