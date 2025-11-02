package com.bytewizard.videoactionservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.videoactionservice.domain.dto.CancelVideoActionRequest;
import com.bytewizard.videoactionservice.domain.dto.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.entity.Like;

public interface LikeService extends IService<Like> {


    Long likeVideo(VideoActionRequest likeVideoRequest);

    Boolean cancelLikeVideo(CancelVideoActionRequest cancelVideoActionRequest);

}

