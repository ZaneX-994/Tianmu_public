package com.bytewizard.videoactionservice.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.common.constants.ErrorCode;
import com.bytewizard.common.constants.SnowflakeConstant;
import com.bytewizard.common.constants.VideoConstant;
import com.bytewizard.common.domain.vo.OnlineBulletResponse;
import com.bytewizard.common.exception.BusinessException;
import com.bytewizard.videoactionservice.domain.dto.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.dto.VideoSubmitRequest;
import com.bytewizard.videoactionservice.domain.entity.*;
import com.bytewizard.videoactionservice.domain.vo.*;
import com.bytewizard.videoactionservice.mapper.VideoMapper;
import com.bytewizard.videoactionservice.service.*;
import com.bytewizard.videoactionservice.utils.BitMapBloomUtil;
import com.bytewizard.videoactionservice.utils.MinioUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zihang
 * @description &#x9488;&#x5BF9;&#x8868;&#x3010;video(&#x89C6;&#x9891;&#x8868;)&#x3011;&#x7684;&#x6570;&#x636E;&#x5E93;&#x64CD;&#x4F5C;Service&#x5B9E;&#x73B0;
 * @createDate 2025-10-23 19:34:01
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService{

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private BitMapBloomUtil bitMapBloomUtil;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserStatsService userStatsService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private VideoStatsService videoStatsService;

    @Autowired
    @Lazy
    private BulletService bulletService;

    @Autowired
    private FollowService followService;

    @Autowired
    @Lazy
    private LikeService likeService;

    @Autowired
    @Lazy
    private CoinService coinService;

    @Autowired
    @Lazy
    private FavoriteService favoriteService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public boolean submit(VideoSubmitRequest videoSubmitRequest) throws Exception {
        // 判断文件大小
        long size = videoSubmitRequest.getFile().getSize();

        if (size > 1024 * 1024) {
            throw new BusinessException(ErrorCode.FILE_SIZE_ERROR);
        }
        String coverUrl = minioUtil.updateCover(videoSubmitRequest.getFile());

        // 判断文件是否存在
        String fileUrl = videoSubmitRequest.getFileUrl();
        if (fileUrl == null
                || fileUrl.isEmpty()
                || !fileService.lambdaQuery().eq(File::getFileUrl, videoSubmitRequest.getFileUrl()).exists()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断用户是否存在
        Long userId = videoSubmitRequest.getUserId();
        if (userId == null || !userService.lambdaQuery().eq(User::getUserId, videoSubmitRequest.getUserId()).exists()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        // 判断视频标题是否存在
        String title = videoSubmitRequest.getTitle();
        if (title == null || title.isEmpty() || StringUtils.isEmpty(title)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断视频类型是否存在
        Integer type = videoSubmitRequest.getType();
        if (type == null || (!type.equals(1) && !type.equals(2))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        // 判断视频时长是否存在
        Double duration = videoSubmitRequest.getDuration();
        if (duration == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断视频分类是否存在
        Integer categoryId = videoSubmitRequest.getCategoryId();
        if (categoryId == null || !categoryService.lambdaQuery().eq(Category::getCategoryId, categoryId).exists()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        // 判断视频标签是否存在
        String tags = videoSubmitRequest.getTags();
        if (tags == null || tags.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断视频是否已经存在
        Snowflake snowflake = IdUtil.getSnowflake(SnowflakeConstant.WORKER_ID, SnowflakeConstant.DATA_CENTER_ID);
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(title);
        video.setType(type);
        video.setDuration(duration);
        video.setCategoryId(categoryId);
        video.setCoverUrl(coverUrl);
        video.setFileUrl(fileUrl);
        video.setTags(tags);
        video.setVideoId(snowflake.nextId());

        // 保存视频
        boolean resultVideo = this.save(video);
        if (!resultVideo) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 保存视频统计
        VideoStats videoStats = new VideoStats();
        videoStats.setVideoId(video.getVideoId());
        boolean resultVideoStats = videoStatsService.save(videoStats);
        if (!resultVideoStats) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        boolean updated = userStatsService.lambdaUpdate().setSql("video_count = video_count + 1").eq(UserStats::getUserId, videoSubmitRequest.getUserId()).update();
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户投稿统计失败");
        }

        // 布隆过滤器添加视频 id
        BitMapBloomUtil.add(video.getVideoId().toString());

        return true;
    }

    @Override
    public List<VideoListResponse> getVideoList(Integer current, Integer pageSize) {

        if (current == null || current <= 0) {
            return Collections.emptyList(); // 无效页码返回空列表
        }

        // 2. 动态调整 pageSize
        // - 第一页（current=1）加载 11 条
        // - 后续页（current>1）加载 15 条
        int dynamicPageSize = (current == 1) ? 11 : 15;

        // 3. 计算偏移量（offset）
        // - 第一页：offset=0（返回 0~10，共11条）
        // - 第二页：offset=11（跳过前11条，返回 11~25，共15条）
        // - 第三页：offset=26（跳过前26条，返回 26~40，共15条）
        int offset = (current == 1) ? 0 : 11 + (current - 2) * 15;
        System.out.println("offset: " + offset + ", dynamicPageSize: " + dynamicPageSize);
        List<VideoListResponse> videoListResponses = videoMapper.selectVideoWithStats(offset, dynamicPageSize);
        System.out.println("anything here????");

        return videoListResponses;

    }

    @Override
    public VideoResponse videoDetail(VideoActionRequest videoActionRequest) {

        // 校验视频是否存在 通过Bloom过滤器 防止缓存穿透
        if (!BitMapBloomUtil.contains(videoActionRequest.getVideoId().toString())) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }

        // 获取视频详情
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getVideoId, videoActionRequest.getVideoId());
        Video video = this.getOne(wrapper);

        // 增加视频观看次数 使用原子操作更新 VideoStats
        boolean updated = videoStatsService.lambdaUpdate().setSql("view_count = view_count + 1").eq(VideoStats::getVideoId, videoActionRequest.getVideoId()).update();
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新视频统计失败");
        }

        if (redisTemplate.hasKey("videoDetails:" + videoActionRequest.getVideoId().toString())) {
            redisTemplate.expire("videoDetails:" + videoActionRequest.getVideoId().toString(), VideoConstant.VIDEO_DETAIL_DAYS, TimeUnit.DAYS);
            return hotVideoDetail(videoActionRequest, video);
        }


        return publicVideoDetail(videoActionRequest, video);
    }

    @Override
    public boolean existsByVideoId(Long videoId) {
        return videoMapper.existsByVideoId(videoId);
    }

    @Override
    @Transactional
    public TripleActionResponse tripleAction(VideoActionRequest videoActionRequest) {

        // 1. 校验用户和视频
        Long videoId = videoActionRequest.getVideoId();
        Long userId = videoActionRequest.getUserId();

        if (!this.lambdaQuery().eq(Video::getVideoId, videoId).exists()) {
            throw new BusinessException(ErrorCode.VIDEO_NOT_FOUND_ERROR);
        }


        if (!userService.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        if (userStatsService.lambdaQuery().eq(UserStats::getUserId, userId).one().getCoinCount() < 1) {
            throw new BusinessException(ErrorCode.USER_COIN_ERROR);
        }

        // 2. 查询是否已三连（1次查询优化）
        boolean hasCoined = coinService.lambdaQuery().eq(Coin::getVideoId, videoId).eq(Coin::getUserId, userId).exists();

        TripleActionResponse response = new TripleActionResponse();
        LambdaUpdateWrapper<VideoStats> statsUpdate = new LambdaUpdateWrapper<VideoStats>().eq(VideoStats::getVideoId, videoId);

        // 投币（原子扣减）
        if (!hasCoined) {
            Coin coin = new Coin();
            coin.setVideoId(videoId);
            coin.setUserId(userId);
            coin.setCoinId(IdUtil.getSnowflake(SnowflakeConstant.WORKER_ID, SnowflakeConstant.DATA_CENTER_ID).nextId());
            if (!coinService.save(coin)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }

            if (!userStatsService.lambdaUpdate().setSql("coin_count = coin_count - 1").eq(UserStats::getUserId, userId).update()) {
                throw new BusinessException(ErrorCode.USER_COIN_ERROR, "投币失败，硬币不足");
            }

            response.setCoin(true);
            statsUpdate.setSql("coin_count = coin_count + 1");
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已经三连过了");
        }

        // 4. 更新视频统计
        if (!videoStatsService.update(statsUpdate)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新视频统计失败");
        }

        return response;
    }

    @Override
    public List<FavoriteVideoResponse> getFavoriteVideoList(Long userId) {
        return videoMapper.getFavoriteVideoList(userId);
    }

    @Override
    public List<VideoListResponse> getLikeVideoList(Long userId) {
        return videoMapper.getLikeVideoList(userId);
    }

    @Override
    public List<VideoListResponse> getCoinVideoList(Long userId) {
        return videoMapper.getCoinVideoList(userId);
    }

    @Override
    public List<VideoListResponse> getCategoryVideoList(Integer categoryId) {
        return videoMapper.getCategoryVideoList(categoryId);
    }

    @Override
    public List<VideoListResponse> getSubmitVideoList(Long userId) {
        return videoMapper.getSubmitVideoList(userId);
    }

    // ######################################## HELPER METHOD ########################################

    public VideoResponse publicVideoDetail(VideoActionRequest videoActionRequest, Video video) {

        // 获取视频详情
        VideoDetailsResponse videoDetails = videoMapper.getVideoDetails(videoActionRequest.getVideoId());

        // 封装响应对象
        VideoResponse videoResponse = new VideoResponse();
        videoResponse.setVideoDetailsResponse(videoDetails);
        videoResponse.setTripleActionResponse(getTripleActionResponse(videoActionRequest));
        videoResponse.setOnlineBulletList(getVideoBullets(videoActionRequest.getVideoId()));
        videoResponse.setVideoRecommendListResponse(getRecommendVideos(video.getCategoryId(), videoActionRequest.getVideoId()));
        videoResponse.setFollow(followService.getFollowType(videoActionRequest.getUserId(), video.getUserId()));


        //判断热点视频
        QueryWrapper<VideoStats> videoStatsQueryWrapper = new QueryWrapper<>();
        videoStatsQueryWrapper.eq("video_id", videoActionRequest.getVideoId());
        VideoStats videoStats = videoStatsService.getOne(videoStatsQueryWrapper);
        if (videoStats.getViewCount() >= VideoConstant.HOT_VIDEO_VIEW_COUNT) {
            Map<String, String> redisVideoDetails = new HashMap<>();
            redisVideoDetails.put("videoId", String.valueOf(videoDetails.getVideoId()));
            redisVideoDetails.put("fileUrl", videoDetails.getFileUrl());
            redisVideoDetails.put("userId", String.valueOf(videoDetails.getUserId()));
            redisVideoDetails.put("title", videoDetails.getTitle());
            redisVideoDetails.put("type", String.valueOf(videoDetails.getType()));
            redisVideoDetails.put("duration", String.valueOf(videoDetails.getDuration()));
            redisVideoDetails.put("tags", videoDetails.getTags());
            redisVideoDetails.put("description", videoDetails.getDescription());
            redisVideoDetails.put("createTime", String.valueOf(videoDetails.getCreateTime().getTime()));
            redisVideoDetails.put("viewCount", String.valueOf(videoDetails.getViewCount()));
            redisVideoDetails.put("bulletCount", String.valueOf(videoDetails.getBulletCount()));
            redisVideoDetails.put("likeCount", String.valueOf(videoDetails.getLikeCount()));
            redisVideoDetails.put("coinCount", String.valueOf(videoDetails.getCoinCount()));
            redisVideoDetails.put("favoriteCount", String.valueOf(videoDetails.getFavoriteCount()));
            redisVideoDetails.put("commentCount", String.valueOf(videoDetails.getCommentCount()));
            redisVideoDetails.put("nickname", videoDetails.getNickname());
            redisVideoDetails.put("avatar", videoDetails.getAvatar());
            redisTemplate.opsForHash().putAll("videoDetails:" + videoActionRequest.getVideoId().toString(), redisVideoDetails);
            redisTemplate.expire("videoDetails:" + videoActionRequest.getVideoId().toString(), VideoConstant.VIDEO_DETAIL_DAYS, TimeUnit.DAYS);

        }
        return videoResponse;
    }

    public VideoResponse hotVideoDetail(VideoActionRequest videoActionRequest, Video video) {

        // 获取视频详情
        Map<String, String> redisVideoDetails = redisTemplate.opsForHash().entries("videoDetails:" + videoActionRequest.getVideoId()).entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString(), (a, b) -> b, HashMap::new));

        VideoDetailsResponse videoDetails = new VideoDetailsResponse();

        // 基本视频信息
        videoDetails.setVideoId(Long.parseLong(redisVideoDetails.get("videoId")));
        videoDetails.setFileUrl(redisVideoDetails.get("fileUrl"));
        videoDetails.setUserId(Long.parseLong(redisVideoDetails.get("userId")));
        videoDetails.setTitle(redisVideoDetails.get("title"));
        videoDetails.setType(Integer.parseInt(redisVideoDetails.get("type")));
        videoDetails.setDuration(Double.parseDouble(redisVideoDetails.get("duration")));
        videoDetails.setTags(redisVideoDetails.get("tags"));
        videoDetails.setDescription(redisVideoDetails.get("description"));
        long timestamp = Long.parseLong(redisVideoDetails.get("createTime"));
        videoDetails.setCreateTime(new Date(timestamp));
        videoDetails.setViewCount(Integer.parseInt(redisVideoDetails.get("viewCount")));
        videoDetails.setBulletCount(Integer.parseInt(redisVideoDetails.get("bulletCount")));
        videoDetails.setLikeCount(Integer.parseInt(redisVideoDetails.get("likeCount")));
        videoDetails.setCoinCount(Integer.parseInt(redisVideoDetails.get("coinCount")));
        videoDetails.setFavoriteCount(Integer.parseInt(redisVideoDetails.get("favoriteCount")));
        videoDetails.setCommentCount(Integer.parseInt(redisVideoDetails.get("commentCount")));
        videoDetails.setNickname(redisVideoDetails.get("nickname"));
        videoDetails.setAvatar(redisVideoDetails.get("avatar"));


        // 封装响应对象
        VideoResponse videoResponse = new VideoResponse();
        videoResponse.setVideoDetailsResponse(videoDetails);
        videoResponse.setTripleActionResponse(getTripleActionResponse(videoActionRequest));
        videoResponse.setOnlineBulletList(getVideoBullets(videoActionRequest.getVideoId()));
        videoResponse.setVideoRecommendListResponse(getRecommendVideos(video.getCategoryId(), video.getVideoId()));
        videoResponse.setFollow(followService.getFollowType(videoActionRequest.getUserId(), video.getUserId()));

        return videoResponse;
    }

    public TripleActionResponse getTripleActionResponse(VideoActionRequest videoActionRequest) {

        TripleActionResponse tripleActionResponse = new TripleActionResponse();
        // 是否点赞
        if (videoActionRequest.getUserId() != null) {
            // 判断是否点赞
            Like likeVideo = likeService.lambdaQuery().eq(Like::getVideoId, videoActionRequest.getVideoId()).eq(Like::getUserId, videoActionRequest.getUserId()).one();
            if (likeVideo != null) {
                tripleActionResponse.setLikeId(likeVideo.getLikeId());
            }

            // 是否收藏
            Favorite favoriteVideo = favoriteService.lambdaQuery().eq(Favorite::getVideoId, videoActionRequest.getVideoId()).eq(Favorite::getUserId, videoActionRequest.getUserId()).one();
            if (favoriteVideo != null) {
                tripleActionResponse.setFavoriteId(favoriteVideo.getFavoriteId());
            }

            // 是否投币
            Coin coinVideo = coinService.lambdaQuery().eq(Coin::getVideoId, videoActionRequest.getVideoId()).eq(Coin::getUserId, videoActionRequest.getUserId()).one();
            if (coinVideo != null) {
                tripleActionResponse.setCoin(true);
            }
        }
        return tripleActionResponse;
    }

    public List<OnlineBulletResponse> getVideoBullets(Long videoId) {
        return bulletService.getBulletList(videoId);
    }

    public List<VideoListResponse> getRecommendVideos(Integer categoryId, Long videoId) {
        return videoMapper.recommendVideoList(categoryId, videoId);
    }
}




