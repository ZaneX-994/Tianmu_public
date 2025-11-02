package com.bytewizard.videoactionservice.controller;

import com.bytewizard.common.domain.BaseResponse;
import com.bytewizard.common.utils.ResultUtils;
import com.bytewizard.videoactionservice.domain.dto.FollowRequest;
import com.bytewizard.videoactionservice.domain.vo.UserListResponse;
import com.bytewizard.videoactionservice.service.FollowService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/video/user")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/follow")
    public BaseResponse<Boolean> follow(@RequestBody FollowRequest followRequest) {
        return ResultUtils.success(followService.follow(followRequest));
    }

    @PostMapping("/cancel/follow")
    public BaseResponse<Boolean> chanelFollow(@RequestBody FollowRequest followRequest) {
        return ResultUtils.success(followService.cancelFollow(followRequest));
    }

    @GetMapping("/following/list")
    public BaseResponse<List<UserListResponse>> followingList(@NotNull(message = "用户id不能为空")  @RequestParam(value = "userId") Long userId) {
        return ResultUtils.success(followService.followList(userId));
    }

    @GetMapping("/followers/list")
    public BaseResponse<List<UserListResponse>> followersList(@NotNull(message = "用户id不能为空") @RequestParam(value = "userId") Long userId) {
        return ResultUtils.success(followService.followerList(userId));
    }

    @PostMapping("/follow/type")
    public Integer followType(@RequestParam Long userId, @RequestParam(value = "creatorId") Long creatorId) {
        return followService.getFollowType(userId, creatorId);
    }

}
