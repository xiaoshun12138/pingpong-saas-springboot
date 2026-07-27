package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.Student;
import com.pingpong.mapper.StudentMapper;
import com.pingpong.service.IStudentService;
import org.springframework.stereotype.Service;

/**
 * 学员 Service 实现类
 * 继承 MyBatis-Plus 的 ServiceImpl，直接使用通用 CRUD 方法。
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {
}
