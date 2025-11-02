package com.bytewizard.videoactionservice.domain.dto;


import lombok.Data;
import java.io.Serializable;

@Data
public class VideoActionRequest implements Serializable {


    /**
     * 用户ID
     */
    private Long userId;


    /**
     * 视频ID
     */
    private Long videoId;
}