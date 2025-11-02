package com.bytewizard.videoactionservice.service;

import com.bytewizard.videoactionservice.domain.dto.FollowRequest;
import com.bytewizard.videoactionservice.domain.vo.UserListResponse;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface FollowService {

    Integer getFollowType(Long userId, Long userId1);

    Boolean follow(FollowRequest followRequest);

    Boolean cancelFollow(FollowRequest followRequest);

    List<UserListResponse> followList(@NotNull(message = "用户id不能为空") Long userId);

    List<UserListResponse> followerList(@NotNull(message = "用户id不能为空") Long userId);
}
