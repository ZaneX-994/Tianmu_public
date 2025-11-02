package com.bytewizard.videoactionservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.videoactionservice.domain.entity.UserStats;
import com.bytewizard.videoactionservice.mapper.UserStatsMapper;
import com.bytewizard.videoactionservice.service.UserStatsService;
import org.springframework.stereotype.Service;

@Service
public class UserStatsServiceImpl extends ServiceImpl<UserStatsMapper, UserStats> implements UserStatsService {

}