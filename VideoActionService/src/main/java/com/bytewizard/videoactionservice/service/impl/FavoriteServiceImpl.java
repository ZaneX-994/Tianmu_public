package com.bytewizard.videoactionservice.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.common.constants.ErrorCode;
import com.bytewizard.common.constants.SnowflakeConstant;
import com.bytewizard.common.exception.BusinessException;
import com.bytewizard.videoactionservice.domain.dto.video.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.entity.Favorite;
import com.bytewizard.videoactionservice.domain.entity.VideoStats;
import com.bytewizard.videoactionservice.mapper.FavoriteMapper;
import com.bytewizard.videoactionservice.service.FavoriteService;
import com.bytewizard.videoactionservice.service.UserService;
import com.bytewizard.videoactionservice.service.VideoService;
import com.bytewizard.videoactionservice.service.VideoStatsService;
import com.bytewizard.videoactionservice.util.CounterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private CounterUtil counterUtil;

    @Autowired
    private VideoStatsService videoStatsService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long favoriteVideo(VideoActionRequest videoActionRequest) {

        // 检测收藏频率是否过快
        crawlerFavoriteDetect(videoActionRequest);

        Long videoId = videoActionRequest.getVideoId();
        Long userId = videoActionRequest.getUserId();

        // 校验判断用户是否存在
        if (!userService.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // 校验判断视频是否存在
        if (!videoService.existsByVideoId(videoId)) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }

        // 查询是否已经收藏
        if (this.lambdaQuery().eq(Favorite::getVideoId, videoId).eq(Favorite::getUserId, userId).exists()) {
            throw new BusinessException(ErrorCode.VIDEO_FAVORITE_ERROR);
        }

        // 保存收藏记录
        Favorite favorite = new Favorite();
        favorite.setFavoriteId(IdUtil.getSnowflake(SnowflakeConstant.WORKER_ID, SnowflakeConstant.DATA_CENTER_ID).nextId());
        favorite.setVideoId(videoId);
        favorite.setUserId(userId);

        if (!this.save(favorite)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 视频收藏数+1
        if (!videoStatsService.lambdaUpdate().setSql("favorite_count = favorite_count + 1").eq(VideoStats::getVideoId, videoId).update()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return favorite.getFavoriteId();
    }

    @Override
    public Boolean cancelFavoriteVideo(VideoActionRequest videoActionRequest) {

        Long videoId = videoActionRequest.getVideoId();
        Long userId = videoActionRequest.getUserId();

        // 校验判断用户是否存在
        if (!userService.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // 校验判断视频是否存在
        if (!videoService.existsByVideoId(videoId)) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }

        // 查询是否已经收藏
        if (!this.lambdaQuery().eq(Favorite::getVideoId, videoId).eq(Favorite::getUserId, userId).exists()) {
            throw new BusinessException(ErrorCode.VIDEO_FAVORITE_NOT_EXISTS);
        }

        boolean removed = this.removeById(videoActionRequest.getVideoId());
        if (!removed) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 视频点赞数 -1
        boolean updated = videoStatsService.lambdaUpdate().setSql("favorite_count = favorite_count - 1").eq(VideoStats::getVideoId, videoId).update();
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return true;
    }

    private void crawlerFavoriteDetect(VideoActionRequest videoActionRequest) {
        // 调用多少次时告警
        final int WARN_COUNT = 2;

        // 拼接访问 key
        String key = String.format("favorite:%s:%s", videoActionRequest.getUserId(), videoActionRequest.getVideoId());

        // 统计一分钟内访问次数，80 秒过期
        long count = counterUtil.incrAndGetCounter(key, 1, TimeUnit.MINUTES, 80);

        if (count > WARN_COUNT) {
            throw new BusinessException(ErrorCode.ACCESS_TOO_FREQUENTLY);
        }

    }


}
