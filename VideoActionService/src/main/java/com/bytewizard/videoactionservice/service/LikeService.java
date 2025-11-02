package com.bytewizard.videoactionservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.videoactionservice.domain.dto.video.CancelVideoActionRequest;
import com.bytewizard.videoactionservice.domain.dto.video.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.entity.Like;

public interface LikeService extends IService<Like> {


    Long likeVideo(VideoActionRequest likeVideoRequest);

    Boolean cancelLikeVideo(CancelVideoActionRequest cancelVideoActionRequest);

}

