package com.bytewizard.videoactionservice.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.common.constants.ErrorCode;
import com.bytewizard.common.constants.SnowflakeConstant;
import com.bytewizard.common.exception.BusinessException;
import com.bytewizard.videoactionservice.domain.dto.video.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.entity.Coin;
import com.bytewizard.videoactionservice.domain.entity.UserStats;
import com.bytewizard.videoactionservice.domain.entity.VideoStats;
import com.bytewizard.videoactionservice.mapper.CoinMapper;
import com.bytewizard.videoactionservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper, Coin> implements CoinService {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserStatsService userStatsService;
    @Autowired
    private VideoStatsService videoStatsService;

    @Override
    public Boolean coinVideo(VideoActionRequest videoActionRequest) {

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

        // 校验判断用户是否已经投币
        if (!this.lambdaQuery().eq(Coin::getVideoId, videoId).eq(Coin::getUserId, userId).exists()) {
            throw new BusinessException(ErrorCode.VIDEO_COIN_ERROR);
        }

        // 获取用户详细信息
        UserStats userStats = userStatsService.getById(userId);

        // 校验判断用户硬币是否足够
        if (userStats.getCoinCount() < 1) {
            throw new BusinessException(ErrorCode.USER_COIN_ERROR);
        }

        // 保存投币记录
        Coin coin = new  Coin();
        coin.setCoinId(IdUtil.getSnowflake(SnowflakeConstant.WORKER_ID, SnowflakeConstant.DATA_CENTER_ID).nextId());
        coin.setUserId(userId);
        coin.setVideoId(videoId);
        if (!this.save(coin)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 视频投币数 +1
        if (!videoStatsService.lambdaUpdate().setSql("coin_count = coin_count + 1").eq(VideoStats::getVideoId, videoId).update()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 用户硬币数 -1
        if (!videoStatsService.lambdaUpdate().setSql("coin_count = coin_count - 1").eq(VideoStats::getVideoId, videoId).update()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return true;
    }


}
