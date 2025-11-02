package com.bytewizard.videoactionservice.mapper;

import com.bytewizard.videoactionservice.domain.entity.Bullet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
* @author zihang
* @description 针对表【bullet(弹幕表)】的数据库操作Mapper
* @createDate 2025-10-23 16:54:40
* @Entity generator.domain.Bullet
*/
@Mapper
public interface BulletMapper extends BaseMapper<Bullet> {
    @Select("SELECT EXISTS(SELECT 1 FROM bullet WHERE bullet_id = #{BulletId})")
    boolean existsByBulletId(Long BulletId);
}




