package com.bytewizard.videoactionservice.domain.dto;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class DeleteBulletRequest {

    /**
     * 弹幕ID
     */
    @TableId
    private Long bulletId;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 弹幕内容
     */
    private String content;


}
