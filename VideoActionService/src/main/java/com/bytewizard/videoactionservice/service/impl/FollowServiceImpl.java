package com.bytewizard.videoactionservice.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.common.constants.ErrorCode;
import com.bytewizard.common.constants.SnowflakeConstant;
import com.bytewizard.common.exception.BusinessException;
import com.bytewizard.videoactionservice.domain.dto.FollowRequest;
import com.bytewizard.videoactionservice.domain.entity.Follow;
import com.bytewizard.videoactionservice.domain.entity.User;
import com.bytewizard.videoactionservice.domain.entity.UserStats;
import com.bytewizard.videoactionservice.domain.vo.UserListResponse;
import com.bytewizard.videoactionservice.mapper.FollowMapper;
import com.bytewizard.videoactionservice.service.FollowService;
import com.bytewizard.videoactionservice.service.UserService;
import com.bytewizard.videoactionservice.service.UserStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserStatsService userStatsService;

    // 0 没有 // 1 关注 // 2 互相关注
    @Override
    public Integer getFollowType(Long userId, Long creatorId) {

        int followType = 0;
        boolean existsFollowing = this.lambdaQuery().eq(Follow::getUserId, userId).eq(Follow::getCreatorId, creatorId).exists();
        boolean existsFollower = this.lambdaQuery().eq(Follow::getUserId, creatorId).eq(Follow::getCreatorId, userId).exists();

        if (existsFollowing && existsFollower) {
            followType = 2;
        } else if (existsFollowing) {
            followType = 1;
        }
        return followType;
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public Boolean follow(FollowRequest followRequest) {

        Long userId = followRequest.getUserId();
        Long creatorId = followRequest.getCreatorId();

        // 查询用户是否存在
        if (!userService.existsByUserId(creatorId) || !userService.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // 关注
        Follow follow = new Follow();
        follow.setUserId(userId);
        follow.setCreatorId(creatorId);
        follow.setFollowId(IdUtil.getSnowflake(SnowflakeConstant.WORKER_ID, SnowflakeConstant.DATA_CENTER_ID).nextId());

        if (!this.save(follow)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "关注失败");
        }

        // 更新粉丝统计
        if (!userStatsService.lambdaUpdate().setSql("followers = followers + 1").eq(UserStats::getUserId, creatorId).update()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新博主粉丝统计失败");
        }

        // 更新关注统计
        if (!userStatsService.lambdaUpdate().setSql("following = following + 1").eq(UserStats::getUserId, userId).update()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户关注统计失败");
        }


        return true;
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public Boolean cancelFollow(FollowRequest followRequest) {
        Long userId = followRequest.getUserId();
        Long creatorId = followRequest.getCreatorId();

        // 查询用户是否存在
        if (!userService.existsByUserId(creatorId) || !userService.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId).eq(Follow::getCreatorId, creatorId);

        // 更新粉丝统计
        if (!userStatsService.lambdaUpdate().setSql("followers = followers - 1").eq(UserStats::getUserId, creatorId).update()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新博主粉丝统计失败");
        }

        // 更新关注统计
        if (!userStatsService.lambdaUpdate().setSql("following = following - 1").eq(UserStats::getUserId, userId).update()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户关注统计失败");
        }


        return this.remove(wrapper);
    }

    @Override
    public List<UserListResponse> followList(Long userId) {

        // 查询用户是否存在
        if (!userService.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // 查询用户是否存在关注
        LambdaQueryWrapper<Follow>  wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId);
        List<Follow> followList = this.list(wrapper);

        if (followList.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询用户信息
        List<User> users = userService.listByIds(followList.stream().map(Follow::getCreatorId).collect(Collectors.toSet()));
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getUserId, user -> user));
        List<UserListResponse> userListResponses = new ArrayList<>();
        for (Follow follow : followList) {
            UserListResponse userListResponse = new UserListResponse();
            userListResponse.setUserId(follow.getCreatorId());
            userListResponse.setAvatar(userMap.get(follow.getCreatorId()).getAvatar());
            userListResponse.setNickname(userMap.get(follow.getCreatorId()).getNickname());
            userListResponse.setDescription(userMap.get(follow.getCreatorId()).getDescription());
            userListResponses.add(userListResponse);
        }


        return userListResponses;
    }

    @Override
    public List<UserListResponse> followerList(Long userId) {

        // 查询用户是否存在
        if (!userService.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // 查询用户是否存在关注
        LambdaQueryWrapper<Follow>  wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getCreatorId, userId);
        List<Follow> followList = this.list(wrapper);

        if (followList.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询用户信息
        List<User> userList = userService.listByIds(followList.stream().map(Follow::getUserId).collect(Collectors.toSet()));
        Map<Long, User> userMap = userList.stream().collect(Collectors.toMap(User::getUserId, user -> user));
        List<UserListResponse> userListResponses = new ArrayList<>();
        for (Follow follow : followList) {
            UserListResponse userListResponse = new UserListResponse();
            userListResponse.setUserId(follow.getUserId());
            userListResponse.setAvatar(userMap.get(follow.getUserId()).getAvatar());
            userListResponse.setNickname(userMap.get(follow.getUserId()).getNickname());
            userListResponse.setDescription(userMap.get(follow.getUserId()).getDescription());
            userListResponses.add(userListResponse);
        }


        return userListResponses;
    }


}
