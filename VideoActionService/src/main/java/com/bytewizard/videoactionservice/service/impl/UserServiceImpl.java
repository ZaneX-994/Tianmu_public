package com.bytewizard.videoactionservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.videoactionservice.domain.entity.User;
import com.bytewizard.videoactionservice.mapper.UserMapper;
import com.bytewizard.videoactionservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public boolean existsByUserId(Long userId) {
        return this.baseMapper.existsByUserId(userId);
    }
}