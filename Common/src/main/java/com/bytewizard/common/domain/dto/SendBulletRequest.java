package com.bytewizard.common.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;

@Data
public class SendBulletRequest implements Serializable {

    /*
    * 弹幕ID
    * */
    private Long bulletId;

    /*
    * 视频ID
    * */
    private Long videoId;

    /*
    * 用户ID
    * */
    private Long userId;

    /*
    * 弹幕内容
    * */
    private String content;

    /*
    * 弹幕所在时间点
    * */
    private Double playbackTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;




}
