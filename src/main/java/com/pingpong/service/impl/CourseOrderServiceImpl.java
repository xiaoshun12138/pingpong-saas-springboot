package com.pingpong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pingpong.entity.CourseOrder;
import com.pingpong.entity.CourseType;
import com.pingpong.entity.Student;
import com.pingpong.mapper.CourseOrderMapper;
import com.pingpong.service.ICourseOrderService;
import com.pingpong.service.ICourseTypeService;
import com.pingpong.service.IStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 课包订单 Service 实现类
 * 继承 MyBatis-Plus 的 ServiceImpl，直接使用通用 CRUD 方法。
 * 课时扣减时由 MyBatis-Plus 乐观锁插件自动处理 version 字段。
 */
@Service
public class CourseOrderServiceImpl extends ServiceImpl<CourseOrderMapper, CourseOrder> implements ICourseOrderService {

    @Autowired
    private IStudentService studentService;

    @Autowired
    private ICourseTypeService courseTypeService;

    @Override
    public void fillNames(CourseOrder order) {
        if (order == null) return;
        if (order.getStudentId() != null) {
            Student s = studentService.getById(order.getStudentId());
            order.setStudentName(s != null ? s.getName() : "-");
        }
        if (order.getCourseTypeId() != null) {
            CourseType ct = courseTypeService.getById(order.getCourseTypeId());
            order.setCourseTypeName(ct != null ? ct.getName() : "-");
        }
    }
}
