package com.bytewizard.videoactionservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytewizard.videoactionservice.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT EXISTS(SELECT 1 FROM user WHERE user_id = #{UserId})")
    boolean existsByUserId(Long UserId);
}
