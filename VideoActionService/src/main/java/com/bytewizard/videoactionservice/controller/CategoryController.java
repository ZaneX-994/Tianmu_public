package com.bytewizard.videoactionservice.controller;

import com.bytewizard.common.domain.BaseResponse;
import com.bytewizard.common.utils.ResultUtils;
import com.bytewizard.videoactionservice.domain.vo.CategoryListResponse;
import com.bytewizard.videoactionservice.domain.vo.VideoListResponse;
import com.bytewizard.videoactionservice.service.CategoryService;
import com.bytewizard.videoactionservice.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/video")
public class CategoryController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category/list")
    public BaseResponse<List<VideoListResponse>> categoryList(@RequestParam Integer categoryId) {
        return ResultUtils.success(videoService.getCategoryVideoList(categoryId));
    }

    @GetMapping("/category")
    public BaseResponse<List<CategoryListResponse>> category() {
        return ResultUtils.success(categoryService.categoryList());
    }
}
