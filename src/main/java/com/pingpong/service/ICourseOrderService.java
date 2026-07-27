package com.pingpong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pingpong.entity.CourseOrder;

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
}
