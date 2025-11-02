package com.bytewizard.videoactionservice.domain.vo;

import com.bytewizard.common.domain.vo.OnlineBulletResponse;
import lombok.Data;

import java.util.List;

@Data
public class VideoResponse {

    private VideoDetailsResponse videoDetailsResponse;

    private List<OnlineBulletResponse> onlineBulletList;

    private TripleActionResponse tripleActionResponse;

    private List<VideoListResponse> videoRecommendListResponse;

    //0 未关注 1 已关注 2 互相关注
    private Integer follow;
}
