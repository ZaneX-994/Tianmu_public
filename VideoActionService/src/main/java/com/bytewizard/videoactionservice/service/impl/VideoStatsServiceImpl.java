package com.bytewizard.videoactionservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.videoactionservice.domain.entity.VideoStats;
import com.bytewizard.videoactionservice.service.VideoStatsService;
import com.bytewizard.videoactionservice.mapper.VideoStatsMapper;
import org.springframework.stereotype.Service;

/**
* @author zihang
* @description 针对表【video_stats(视频数据统计表)】的数据库操作Service实现
* @createDate 2025-10-25 16:06:49
*/
@Service
public class VideoStatsServiceImpl extends ServiceImpl<VideoStatsMapper, VideoStats> implements VideoStatsService{

}




