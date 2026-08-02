package com.pingpong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pingpong.entity.LessonSchedule;

/**
 * 排课记录 Service 接口
 * 继承 MyBatis-Plus 的 IService，提供基础 CRUD + 批量操作能力。
 */
public interface ILessonScheduleService extends IService<LessonSchedule> {

    /**
     * 排课 + 自动消课（同一事务）
     * 保存排课记录后自动扣减 1 课时，失败全回滚
     */
    void saveAndConsume(LessonSchedule schedule);

    /**
     * 取消排课 + 归还课时（同一事务）
     * 逻辑删除排课记录，同时归还已扣的 1 课时到订单和学员
     */
    void cancelAndRefund(Long scheduleId);
}
