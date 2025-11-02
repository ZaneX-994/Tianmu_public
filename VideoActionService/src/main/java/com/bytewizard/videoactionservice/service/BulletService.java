package com.bytewizard.videoactionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.common.domain.dto.SendBulletRequest;
import com.bytewizard.common.domain.vo.OnlineBulletResponse;
import com.bytewizard.videoactionservice.domain.dto.DeleteBulletRequest;
import com.bytewizard.videoactionservice.domain.entity.Bullet;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
* @author zihang
* @description 针对表【bullet(弹幕表)】的数据库操作Service
* @createDate 2025-10-23 16:54:40
*/
public interface BulletService extends IService<Bullet> {

    void saveBullet(SendBulletRequest request);

    boolean deleteBullet(@RequestBody DeleteBulletRequest request);

    List<OnlineBulletResponse> getBulletList(Long videoId);

    boolean existsByBulletId(Long bulletId);

    boolean bulletExists(Long bulletId);
}
