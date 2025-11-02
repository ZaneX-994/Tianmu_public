package com.bytewizard.videoactionservice.controller;

import com.bytewizard.common.domain.BaseResponse;
import com.bytewizard.common.utils.ResultUtils;
import com.bytewizard.videoactionservice.domain.dto.CreateCommentRequest;
import com.bytewizard.videoactionservice.domain.dto.video.CancelVideoActionRequest;
import com.bytewizard.videoactionservice.domain.dto.video.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.dto.video.VideoSubmitRequest;
import com.bytewizard.videoactionservice.domain.vo.*;
import com.bytewizard.videoactionservice.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CoinService coinService;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CommentService commentService;

    @PostMapping("/submit")
    public BaseResponse<Boolean> submit(@RequestBody VideoSubmitRequest request) throws Exception {
        return ResultUtils.success(videoService.submit(request));
    }

    @GetMapping("/list")
    public BaseResponse<List<VideoListResponse>> videoList(@RequestParam(value = "current") Integer current, @RequestParam(value = "pageSize") Integer pageSize) {
        return ResultUtils.success(videoService.getVideoList(current, pageSize));
    }

    @PostMapping("/detail")
    public BaseResponse<VideoResponse> videoDetail(@RequestBody VideoActionRequest videoActionRequest) {
        return ResultUtils.success(videoService.videoDetail(videoActionRequest));
    }

    @GetMapping("/submit/list")
    public BaseResponse<List<VideoListResponse>> submitVideoList(@Valid @NotNull(message = "用户ID不能为空") @RequestParam(value = "userId") Long userId) {
        return ResultUtils.success(videoService.getSubmitVideoList(userId));
    }

    @PostMapping("/like")
    public BaseResponse<Long> likeVideo(@RequestBody VideoActionRequest videoActionRequest) {
        return ResultUtils.success(likeService.likeVideo(videoActionRequest));
    }

    @PostMapping("/cancel/like")
    public BaseResponse<Boolean> cancelLikeVideo(@RequestBody CancelVideoActionRequest cancelVideoActionRequest) {
        return ResultUtils.success(likeService.cancelLikeVideo(cancelVideoActionRequest));
    }

    @PostMapping("/coin")
    public BaseResponse<Boolean> coinVideo(@RequestBody VideoActionRequest videoActionRequest) {
        return  ResultUtils.success(coinService.coinVideo(videoActionRequest));
    }

    @PostMapping("/favorite")
    public BaseResponse<Long>  favoriteVideo(@RequestBody VideoActionRequest videoActionRequest) {
        return ResultUtils.success(favoriteService.favoriteVideo(videoActionRequest));
    }

    @PostMapping("/cancel/favorite")
    public BaseResponse<Boolean> cancelFavoriteVideo(@RequestBody VideoActionRequest videoActionRequest) {
        return ResultUtils.success(favoriteService.cancelFavoriteVideo(videoActionRequest));
    }

    @PostMapping("/create/comment")
    public BaseResponse<CommentResponse> createCommentVideo(@RequestBody CreateCommentRequest createCommentRequest) {
        return ResultUtils.success(commentService.createCommentVideo(createCommentRequest));
    }

    @PostMapping("/delete/comment")
    public BaseResponse<Boolean> deleteCommentVideo(@RequestBody CancelVideoActionRequest cancelVideoActionRequest) {
        return ResultUtils.success(commentService.deleteCommentVideo(cancelVideoActionRequest));
    }

    @GetMapping("/comment/list")
    public BaseResponse<List<CommentVideoResponse>> getCommentVideoList(@NotNull(message = "视频ID不能为空") @RequestParam Long videoId) {
        return ResultUtils.success(commentService.getCommentVideoList(videoId));
    }

    @PostMapping("/triple/action")
    public BaseResponse<TripleActionResponse> tripleAction(@RequestBody VideoActionRequest videoActionRequest) {
        return ResultUtils.success(videoService.tripleAction(videoActionRequest));
    }

    @GetMapping("/favorite/list")
    public BaseResponse<List<FavoriteVideoResponse>> favoriteVideoList(@Valid @NotNull(message = "用户ID不能为空") @RequestParam Long userId) {
        return ResultUtils.success(videoService.getFavoriteVideoList(userId));
    }

    @GetMapping("/like/list")
    public BaseResponse<List<VideoListResponse>> likeVideoList(@Valid @NotNull(message = "用户ID不能为空") @RequestParam Long userId) {
        return ResultUtils.success(videoService.getLikeVideoList(userId));
    }

    @GetMapping("/coin/list")
    public BaseResponse<List<VideoListResponse>> coinVideoList(@Valid @NotNull(message = "用户ID不能为空") @RequestParam Long userId) {
        return ResultUtils.success(videoService.getCoinVideoList(userId));
    }

}
