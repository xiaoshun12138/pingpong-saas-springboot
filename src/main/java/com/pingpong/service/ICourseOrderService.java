package com.pingpong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pingpong.entity.CourseOrder;
import com.pingpong.entity.Student;

/**
 * 课包订单 Service 接口
 * 继承 MyBatis-Plus 的 IService，提供基础 CRUD + 批量操作能力。
 * 订单的课时扣减使用乐观锁保证并发安全。
 */
public interface ICourseOrderService extends IService<CourseOrder> {

    /**
     * 填充关联名称（学员名、课包名）
     * @param order 订单
     */
    void fillNames(CourseOrder order);

    /**
     * 新建订单 + 创建新学员 + 累加学员总课时（同一事务）
     */
    void createOrderWithNewStudent(CourseOrder order, Student student);

    /**
     * 续费订单 + 累加学员总课时（同一事务）
     */
    void renewOrder(CourseOrder order, Student student);
}
