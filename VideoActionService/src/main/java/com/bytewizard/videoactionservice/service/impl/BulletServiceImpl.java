package com.bytewizard.videoactionservice.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.common.constants.ErrorCode;
import com.bytewizard.common.domain.dto.SendBulletRequest;
import com.bytewizard.common.domain.vo.OnlineBulletResponse;
import com.bytewizard.common.exception.BusinessException;
import com.bytewizard.videoactionservice.domain.dto.DeleteBulletRequest;
import com.bytewizard.videoactionservice.domain.entity.Bullet;
import com.bytewizard.videoactionservice.domain.entity.User;
import com.bytewizard.videoactionservice.domain.entity.Video;
import com.bytewizard.videoactionservice.domain.entity.VideoStats;
import com.bytewizard.videoactionservice.mapper.BulletMapper;
import com.bytewizard.videoactionservice.service.BulletService;
import com.bytewizard.videoactionservice.service.UserService;
import com.bytewizard.videoactionservice.service.VideoService;
import com.bytewizard.videoactionservice.service.VideoStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author zihang
 * @description 针对表【bullet(弹幕表)】的数据库操作Service实现
 * @createDate 2025-10-23 16:54:40
 */
@Service
public class BulletServiceImpl extends ServiceImpl<BulletMapper, Bullet> implements BulletService {

    @Autowired
    private VideoService videoService;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBullet(SendBulletRequest request) {
        Long userId = request.getUserId();
        Long videoId = request.getVideoId();

        // 校验视频是否存在
        if (!checkIfVideoExists(videoId)){
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }

        // 校验用户是否存在
        if (!checkIfUserExists(userId)){
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // TODO: 使用原子操作更新VideoStats
        boolean updated = videoStatsService.lambdaUpdate().setSql("bullet_count = bullet_count + 1").eq(VideoStats::getVideoId, videoId).update();
        if (!updated){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新视频统计失败");
        }


        // 保存视频
        Bullet bullet = new Bullet();
        bullet.setUserId(userId);
        bullet.setVideoId(videoId);
        bullet.setContent(request.getContent());
        bullet.setPlaybackTime(request.getPlaybackTime());
        bullet.setCreateTime(DateTime.now());

        if (!this.save(bullet)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存弹幕失败");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBullet(DeleteBulletRequest request) {

        Long bulletId = request.getBulletId();
        Long userId = request.getUserId();
        Long videoId = request.getVideoId();

        // TODO: 校验视频是否存在: 优化为exists查询
        if (!videoService.existsByVideoId(videoId)) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }

        // TODO: 校验用户是否存在
        if (!userService.existsByUserId(userId)){
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // TODO: 校验弹幕是否存在
        if (!this.existsByBulletId(bulletId)) {
            throw new BusinessException(ErrorCode.BULLET_NOT_EXISTS);
        }

        // TODO: 使用原子操作更新VideoStats
        boolean updated = videoStatsService.lambdaUpdate().setSql("bullet_count = bullet_count - 1").eq(VideoStats::getVideoId, videoId).update();
        if (!updated){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新视频统计失败");
        }

        // 保存弹幕
        if (!this.removeById(bulletId)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除弹幕失败");
        }

        return true;
    }

    @Override
    public List<OnlineBulletResponse> getBulletList(Long videoId) {

        List<OnlineBulletResponse> onlineBulletResponses = new ArrayList<>();
        String cacheKey = "video:" + videoId + ":bullet";

        if (redisTemplate.hasKey(cacheKey)) {

            Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().rangeWithScores(cacheKey, 0, -1);
            System.out.println(tuples);

            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String[] parts = tuple.getValue().split(":");
                OnlineBulletResponse onlineBulletResponse = new OnlineBulletResponse();
                onlineBulletResponse.setUserId(parts[0]);
                onlineBulletResponse.setBulletId(parts[1]);
                onlineBulletResponse.setText(parts[2]);
                onlineBulletResponse.setPlaybackTime(tuple.getScore());
                onlineBulletResponses.add(onlineBulletResponse);
            }

        } else {

            LambdaQueryWrapper<Bullet> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Bullet::getVideoId, videoId);
            List<Bullet> bullets = this.list(wrapper);

            if (bullets.isEmpty()) {
                return onlineBulletResponses;
            }

            Set<ZSetOperations.TypedTuple<String>> addTuples = new HashSet<>();

            for (Bullet bullet : bullets) {
                String bulletId = bullet.getBulletId().toString();
                String userId = bullet.getUserId().toString();
                String content = bullet.getContent();
                Double playbackTime = bullet.getPlaybackTime();
                OnlineBulletResponse onlineBulletResponse = new OnlineBulletResponse();
                onlineBulletResponse.setText(content);
                onlineBulletResponse.setPlaybackTime(playbackTime);
                onlineBulletResponse.setBulletId(bulletId);
                onlineBulletResponse.setUserId(userId);
                onlineBulletResponses.add(onlineBulletResponse);
                addTuples.add(new DefaultTypedTuple<>(userId + ":" + bulletId + ":" + content, playbackTime));
            }

            try {
                redisTemplate.opsForZSet().add(cacheKey, addTuples);
                // 随机设置过期时间，防止缓存雪崩
                redisTemplate.expire(cacheKey, 72 * 3600 + ThreadLocalRandom.current().nextInt(3600), TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException("Redis 保存弹幕失败");
            }
        }

        // 对弹幕按时间排序
        onlineBulletResponses.sort(Comparator.comparingDouble(OnlineBulletResponse::getPlaybackTime));

        return onlineBulletResponses;

    }

    @Override
    public boolean existsByBulletId(Long bulletId) {

        return this.baseMapper.existsByBulletId(bulletId);
    }

    @Override
    public boolean bulletExists(Long bulletId) {
        return this.lambdaQuery().eq(Bullet::getBulletId, bulletId).exists();
    }

    // ########## HELPER METHODS ##########

    public boolean checkIfVideoExists(Long videoId) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getVideoId, videoId);

        Video video = videoService.getOne(wrapper);

        return video != null;

    }

    public boolean checkIfUserExists(Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserId, userId);

        User user = userService.getOne(wrapper);

        return user != null;

    }
}




