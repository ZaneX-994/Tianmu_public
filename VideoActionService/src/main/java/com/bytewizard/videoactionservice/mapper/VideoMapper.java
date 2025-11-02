package com.bytewizard.videoactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytewizard.videoactionservice.domain.entity.Video;
import com.bytewizard.videoactionservice.domain.vo.FavoriteVideoResponse;
import com.bytewizard.videoactionservice.domain.vo.VideoDetailsResponse;
import com.bytewizard.videoactionservice.domain.vo.VideoListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
* @author zihang
* @description 针对表【video(视频表)】的数据库操作Mapper
* @createDate 2025-10-23 19:34:01
* @Entity com.bytewizard.realtimeservice.domain.entity.Video
*/
@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    List<VideoListResponse> selectVideoWithStats(@Param("current") Integer current, @Param("pageSize") Integer pageSize);


    VideoDetailsResponse getVideoDetails(Long videoId);

    @Select("SELECT EXISTS(SELECT 1 FROM video WHERE video_id = #{videoId})")
    boolean existsByVideoId(Long videoId);

    List<VideoListResponse> recommendVideoList(@Param("categoryId") Integer categoryId, @Param("videoId") Long vid);

    List<VideoListResponse> getLikeVideoList(Long userId);

    List<VideoListResponse> getCoinVideoList(Long userId);

    List<FavoriteVideoResponse> getFavoriteVideoList(Long userId);

    List<VideoListResponse> getSubmitVideoList(Long userId);

    List<VideoListResponse> getCategoryVideoList(Integer categoryId);
}




