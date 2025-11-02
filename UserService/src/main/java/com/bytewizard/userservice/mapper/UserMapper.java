package com.bytewizard.userservice.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytewizard.userservice.domain.entity.User;
import com.bytewizard.userservice.domain.vo.LoginResponse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {


    LoginResponse getUserInfo(Long userId);
}