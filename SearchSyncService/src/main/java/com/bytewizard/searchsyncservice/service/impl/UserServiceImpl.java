package com.bytewizard.searchsyncservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.bytewizard.searchsyncservice.mapper.UserMapper;
import com.bytewizard.searchsyncservice.domain.entity.User;
import com.bytewizard.searchsyncservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {



}