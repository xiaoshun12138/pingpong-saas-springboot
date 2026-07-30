package com.pingpong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pingpong.entity.RefundLog;

/**
 * 退款记录 Service 接口
 * 继承 MyBatis-Plus 的 IService，提供基础 CRUD + 批量操作能力。
 * 核心业务方法 refund 以事务方式完成退款全流程。
 */
public interface  IRefundLogService extends IService<RefundLog> {

    /**
     * 执行退款操作（事务保证原子性）
     * 包含三步：1. 插入退款日志 2. 订单状态置为 refunded 并清零剩余课时（乐观锁） 3. 扣减学员总剩余课时
     * 任一步骤失败都会整体回滚
     *
     * @param refundLog 退款记录信息
     */
    void refund(RefundLog refundLog);

    /**
     * 填充关联名称（学员名、订单编号、操作人名）
     * @param refundLog 退款记录
     */
    void fillNames(RefundLog refundLog);
}
