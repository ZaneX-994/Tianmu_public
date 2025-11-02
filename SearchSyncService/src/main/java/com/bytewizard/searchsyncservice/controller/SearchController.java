package com.bytewizard.searchsyncservice.controller;

import java.util.List;
import java.util.stream.Collectors;


import com.bytewizard.common.constants.UserConstant;
import com.bytewizard.common.constants.VideoConstant;
import com.bytewizard.common.domain.BaseResponse;
import com.bytewizard.common.utils.ResultUtils;
import com.bytewizard.searchsyncservice.domain.es.UserEs;
import com.bytewizard.searchsyncservice.domain.es.VideoEs;
import com.bytewizard.searchsyncservice.domain.vo.SearchUserListResponse;
import com.bytewizard.searchsyncservice.domain.vo.SearchVideoListResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    @GetMapping("/api/search/user")
    public BaseResponse<List<SearchUserListResponse>> searchUser(@NotNull(message = "关键字不能为空") @RequestParam String keyword) {
        // 1. 创建Criteria查询（最稳定的方式）
        Criteria criteria = new Criteria("nickname").matches(keyword);
        Query searchQuery = new CriteriaQuery(criteria);

        // 2. 执行搜索
        SearchHits<UserEs> searchHits = elasticsearchTemplate.search(searchQuery, UserEs.class, IndexCoordinates.of(UserConstant.USER_ES_INDEX));

        // 3. 处理结果
        List<SearchUserListResponse> userListResponses = searchHits.stream().map(SearchHit::getContent).map(this::convertUserToResponse).collect(Collectors.toList());

        return ResultUtils.success(userListResponses);


    }


    private SearchUserListResponse convertUserToResponse(UserEs userEs) {
        SearchUserListResponse response = new SearchUserListResponse();
        response.setAvatar(userEs.getAvatar());
        response.setDescription(userEs.getDescription());
        response.setFollowers(userEs.getFollowers());
        response.setNickname(userEs.getNickname());
        response.setUserId(userEs.getId());
        response.setVideoCount(userEs.getVideoCount());
        return response;
    }


    @GetMapping("/search/video")
    public BaseResponse<List<SearchVideoListResponse>> searchVideo(@NotNull(message = "关键字不能为空")  @RequestParam String keyword) {

        // 1. 创建Criteria查询
        Criteria criteria = new Criteria("title").matches(keyword);
        Query searchQuery = new CriteriaQuery(criteria);

        // 2. 执行搜索
        SearchHits<VideoEs> searchHits = elasticsearchTemplate.search(searchQuery, VideoEs.class, IndexCoordinates.of(VideoConstant.VIDEO_ES_INDEX));

        // 3. 处理搜索结果
        List<SearchVideoListResponse> videoListResponses = searchHits.stream().map(SearchHit::getContent).map(this::convertVideoToResponse).collect(Collectors.toList());

        return ResultUtils.success(videoListResponses);


    }

    private SearchVideoListResponse convertVideoToResponse(VideoEs videoEs) {
        SearchVideoListResponse response = new SearchVideoListResponse();
        response.setBulletCount(videoEs.getBulletCount());
        response.setCoverUrl(videoEs.getCoverUrl());
        response.setCreateTime(videoEs.getCreateTime());
        response.setDuration(videoEs.getDuration());
        response.setFileUrl(videoEs.getFileUrl());
        response.setNickName(videoEs.getNickName());
        response.setTitle(videoEs.getTitle());
        response.setUserId(videoEs.getUserId());
        response.setVideoId(videoEs.getId());
        response.setViewCount(videoEs.getViewCount());
        return response;
    }



}