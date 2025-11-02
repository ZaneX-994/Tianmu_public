package com.bytewizard.userservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.userservice.domain.entity.UserStats;
import com.bytewizard.userservice.mapper.UserStatsMapper;
import com.bytewizard.userservice.service.UserStatsService;
import org.springframework.stereotype.Service;

@Service
public class UserStatsServiceImpl extends ServiceImpl<UserStatsMapper, UserStats> implements UserStatsService {

}