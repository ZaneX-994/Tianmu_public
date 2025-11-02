package com.bytewizard.videoactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytewizard.videoactionservice.domain.entity.VideoStats;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zihang
* @description 针对表【video_stats(视频数据统计表)】的数据库操作Mapper
* @createDate 2025-10-25 16:06:49
* @Entity com.bytewizard.videoactionservice.domain.entity.VideoStats
*/
@Mapper
public interface VideoStatsMapper extends BaseMapper<VideoStats> {

}




