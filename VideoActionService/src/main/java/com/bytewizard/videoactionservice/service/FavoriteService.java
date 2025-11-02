package com.bytewizard.videoactionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.videoactionservice.domain.dto.video.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.entity.Favorite;

public interface FavoriteService extends IService<Favorite> {

    Long favoriteVideo(VideoActionRequest videoActionRequest);

    Boolean cancelFavoriteVideo(VideoActionRequest videoActionRequest);
}
