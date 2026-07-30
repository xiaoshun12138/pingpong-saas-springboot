package com.pingpong.service;

import com.pingpong.dto.TargetDashboardVO;

/**
 * 目标仪表盘 Service 接口
 * 提供业绩目标和课消目标的综合统计看板数据。
 */
public interface ITargetDashboardService {

    /**
     * 业绩目标仪表盘数据
     *
     * @param year    年份
     * @param month   月份（1-12），默认当前月
     * @param storeId 门店ID，为 null 时查询全部门店
     * @return 业绩目标综合数据
     */
    TargetDashboardVO salesTargetDashboard(Integer year, Integer month, Long storeId);

    /**
     * 课消目标仪表盘数据
     *
     * @param year    年份
     * @param month   月份（1-12），默认当前月
     * @param storeId 门店ID，为 null 时查询全部门店
     * @return 课消目标综合数据
     */
    TargetDashboardVO consumptionTargetDashboard(Integer year, Integer month, Long storeId);
}
