package com.bytewizard.userservice.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytewizard.userservice.domain.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {

}