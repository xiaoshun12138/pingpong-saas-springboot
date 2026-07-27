package com.pingpong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pingpong.entity.CourseConsumption;

/**
 * 消课记录 Service 接口
 * 继承 MyBatis-Plus 的 IService，提供基础 CRUD + 批量操作能力。
 * 核心业务方法 consumeLesson 以事务方式完成消课全流程。
 */
public interface ICourseConsumptionService extends IService<CourseConsumption> {

    /**
     * 执行消课操作（事务保证原子性）
     * 包含三步：1. 插入消课记录 2. 扣减订单剩余课时（乐观锁） 3. 扣减学员总剩余课时
     * 任一步骤失败都会整体回滚
     *
     * @param consumption 消课记录信息
     */
    void consumeLesson(CourseConsumption consumption);

    /**
     * 填充关联名称（学员名、教练名、订单编号）
     * @param consumption 消课记录
     */
    void fillNames(CourseConsumption consumption);
}
