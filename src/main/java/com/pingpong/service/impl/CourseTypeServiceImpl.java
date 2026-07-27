package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.CourseType;
import com.pingpong.mapper.CourseTypeMapper;
import com.pingpong.service.ICourseTypeService;
import org.springframework.stereotype.Service;

/**
 * 课包类型 Service 实现类
 * 继承 MyBatis-Plus 的 ServiceImpl，直接使用通用 CRUD 方法。
 */
@Service
public class CourseTypeServiceImpl extends ServiceImpl<CourseTypeMapper, CourseType> implements ICourseTypeService {
}
