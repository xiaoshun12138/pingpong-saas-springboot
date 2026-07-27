package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.MonthlyTarget;
import com.pingpong.mapper.MonthlyTargetMapper;
import com.pingpong.service.IMonthlyTargetService;
import org.springframework.stereotype.Service;

/**
 * 月度目标 Service 实现类
 * 继承 MyBatis-Plus 的 ServiceImpl，直接使用通用 CRUD 方法。
 */
@Service
public class MonthlyTargetServiceImpl extends ServiceImpl<MonthlyTargetMapper, MonthlyTarget> implements IMonthlyTargetService {
}
