package com.bytewizard.videoactionservice.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 分区表
 * @TableName category
 */
@TableName(value ="category")
@Data
public class Category {
    /**
     * 分区id
     */
    @TableId(type = IdType.AUTO)
    private Integer categoryId;

    /**
     * 分区名
     */
    private String categoryName;

    /**
     * 
     */
    private Date createTime;
}