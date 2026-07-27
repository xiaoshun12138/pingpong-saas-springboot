package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.LessonSchedule;
import com.pingpong.mapper.LessonScheduleMapper;
import com.pingpong.service.ILessonScheduleService;
import org.springframework.stereotype.Service;

/**
 * 排课记录 Service 实现类
 * 继承 MyBatis-Plus 的 ServiceImpl，直接使用通用 CRUD 方法。
 */
@Service
public class LessonScheduleServiceImpl extends ServiceImpl<LessonScheduleMapper, LessonSchedule> implements ILessonScheduleService {
}
