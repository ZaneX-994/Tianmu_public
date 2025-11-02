package com.bytewizard.videoactionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.videoactionservice.domain.dto.video.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.dto.video.VideoSubmitRequest;
import com.bytewizard.videoactionservice.domain.entity.Video;
import com.bytewizard.videoactionservice.domain.vo.FavoriteVideoResponse;
import com.bytewizard.videoactionservice.domain.vo.TripleActionResponse;
import com.bytewizard.videoactionservice.domain.vo.VideoListResponse;
import com.bytewizard.videoactionservice.domain.vo.VideoResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
* @author zihang
* @description 针对表【video(视频表)】的数据库操作Service
* @createDate 2025-10-23 19:34:01
*/
public interface VideoService extends IService<Video> {

    boolean submit(VideoSubmitRequest videoSubmitRequest) throws Exception;

    List<VideoListResponse> getVideoList(Integer current, Integer pageSize);

    VideoResponse videoDetail(VideoActionRequest videoActionRequest);

    boolean existsByVideoId(Long videoId);

    TripleActionResponse tripleAction(VideoActionRequest videoActionRequest);

    List<FavoriteVideoResponse> getFavoriteVideoList(@Valid @NotNull(message = "用户ID不能为空") Long userId);

    List<VideoListResponse> getLikeVideoList(@Valid @NotNull(message = "用户ID不能为空") Long userId);

    List<VideoListResponse> getCoinVideoList(@Valid @NotNull(message = "用户ID不能为空") Long userId);

    List<VideoListResponse> getCategoryVideoList(Integer categoryId);

    List<VideoListResponse> getSubmitVideoList(@Valid @NotNull(message = "用户ID不能为空") Long userId);
}
