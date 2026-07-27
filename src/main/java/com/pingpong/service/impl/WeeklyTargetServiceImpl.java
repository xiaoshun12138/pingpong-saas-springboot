package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.WeeklyTarget;
import com.pingpong.mapper.WeeklyTargetMapper;
import com.pingpong.service.IWeeklyTargetService;
import org.springframework.stereotype.Service;

/**
 * 周度目标 Service 实现类
 * 继承 MyBatis-Plus 的 ServiceImpl，直接使用通用 CRUD 方法。
 */
@Service
public class WeeklyTargetServiceImpl extends ServiceImpl<WeeklyTargetMapper, WeeklyTarget> implements IWeeklyTargetService {
}
