package com.bytewizard.videoactionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.videoactionservice.domain.dto.VideoActionRequest;
import com.bytewizard.videoactionservice.domain.entity.Coin;

public interface CoinService extends IService<Coin> {
    Boolean coinVideo(VideoActionRequest videoActionRequest);
}
