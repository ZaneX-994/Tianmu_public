package com.bytewizard.videoactionservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.videoactionservice.domain.entity.User;

public interface UserService extends IService<User> {

    boolean existsByUserId(Long userId);
}
