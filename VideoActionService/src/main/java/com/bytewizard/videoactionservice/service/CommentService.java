package com.bytewizard.videoactionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.videoactionservice.domain.dto.CancelVideoActionRequest;
import com.bytewizard.videoactionservice.domain.dto.CreateCommentRequest;
import com.bytewizard.videoactionservice.domain.entity.Comment;
import com.bytewizard.videoactionservice.domain.vo.CommentResponse;
import com.bytewizard.videoactionservice.domain.vo.CommentVideoResponse;
import jakarta.validation.constraints.NotNull;

import java.util.List;


public interface CommentService extends IService<Comment> {

    CommentResponse createCommentVideo(CreateCommentRequest createCommentRequest);

    Boolean deleteCommentVideo(CancelVideoActionRequest cancelVideoActionRequest);

    List<CommentVideoResponse> getCommentVideoList(@NotNull(message = "视频ID不能为空") Long videoId);
}
