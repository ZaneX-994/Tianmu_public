package com.bytewizard.videoactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytewizard.videoactionservice.domain.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
* @author zihang
* @description 针对表【category(分区表)】的数据库操作Mapper
* @createDate 2025-10-25 15:58:19
* @Entity com.bytewizard.videoactionservice.domain.entity.Category
*/
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}




