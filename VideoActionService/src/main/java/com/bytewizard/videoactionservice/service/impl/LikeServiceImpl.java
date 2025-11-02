package com.bytewizard.videoactionservice.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.common.constants.ErrorCode;
import com.bytewizard.common.constants.SnowflakeConstant;
import com.bytewizard.common.exception.BusinessException;
import com.bytewizard.videoactionservice.domain.dto.CancelVideoActionRequest;
import com.bytewizard.videoactionservice.domain.dto.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.entity.Like;
import com.bytewizard.videoactionservice.domain.entity.User;
import com.bytewizard.videoactionservice.domain.entity.Video;
import com.bytewizard.videoactionservice.domain.entity.VideoStats;
import com.bytewizard.videoactionservice.mapper.LikeMapper;
import com.bytewizard.videoactionservice.service.LikeService;
import com.bytewizard.videoactionservice.service.UserService;
import com.bytewizard.videoactionservice.service.VideoService;
import com.bytewizard.videoactionservice.service.VideoStatsService;
import com.bytewizard.videoactionservice.utils.CounterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private UserService userService;

    @Autowired
    private CounterUtil counterUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long likeVideo(VideoActionRequest request) {

        // 检测点赞频率是否过快
        crawlerLikeDetect(request);

        // 校验判断视频是否存在
        if (!videoService.lambdaQuery().eq(Video::getVideoId, request.getVideoId()).exists()) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }

        // 校验判断用户是否存在
        if (!userService.lambdaQuery().eq(User::getUserId, request.getUserId()).exists()) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // 查询是否已经点赞
        if (this.lambdaQuery()
                .eq(Like::getVideoId, request.getVideoId())
                .eq(Like::getUserId, request.getUserId()).exists()) {
            throw new BusinessException(ErrorCode.VIDEO_LIKED_ERROR);
        }

        // 保存点赞记录
        Like like = new Like();
        like.setLikeId(IdUtil.getSnowflake(SnowflakeConstant.WORKER_ID, SnowflakeConstant.DATA_CENTER_ID).nextId());
        like.setUserId(request.getUserId());
        like.setVideoId(request.getVideoId());
        boolean save = this.save(like);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 视频点赞数+1
        boolean updated = videoStatsService.lambdaUpdate().setSql("like_count = like_count + 1").eq(VideoStats::getVideoId, like.getVideoId()).update();
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新视频统计失败");
        }

        return like.getLikeId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelLikeVideo(CancelVideoActionRequest request) {

        // 校验判断视频是否存在
        if (!videoService.lambdaQuery().eq(Video::getVideoId, request.getVideoId()).exists()) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }

        // 删除点赞记录
        boolean removed = this.removeById(request.getVideoId());
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 视频点赞数 -1
        boolean updated = videoService.lambdaUpdate().setSql("like_count = like_count - 1").eq(Video::getVideoId, request.getVideoId()).update();
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新视频点赞统计失败");
        }

        return true;
    }

    private void crawlerLikeDetect(VideoActionRequest videoActionRequest) {
        // 调用多少次时告警
        final int WARN_COUNT = 2;
        // 拼接访问 key
        String key = String.format("like:%s:%s", videoActionRequest.getUserId(), videoActionRequest.getVideoId());
        // 统计一分钟内访问次数，80 秒过期
        long count = counterUtil.incrAndGetCounter(key, 1, TimeUnit.MINUTES, 80);

        // 是否告警
        if (count > WARN_COUNT) {
            throw new BusinessException(ErrorCode.ACCESS_TOO_FREQUENTLY);
        }
    }
}
