package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 仪表盘总览数据 VO（View Object）
 * 封装首页看板展示的核心统计指标，包含门店规模、本月经营数据和活跃订单等。
 */
@Data
@AllArgsConstructor
public class DashboardVO {

    /** 门店总数 */
    private long storeCount;

    /** 员工总数 */
    private long staffCount;

    /** 学员总数 */
    private long studentCount;

    /** 本月消课次数（记录条数） */
    private long monthConsumptionCount;

    /** 本月消课总课时数 */
    private long monthConsumptionLessons;

    /** 本月新增订单数 */
    private long monthNewOrderCount;

    /** 本月新增订单总金额 */
    private BigDecimal monthNewOrderAmount;

    /** 本月退款笔数 */
    private long monthRefundCount;

    /** 本月退款总金额 */
    private BigDecimal monthRefundAmount;

    /** 活跃订单数（剩余课时 > 0 的进行中订单） */
    private long activeOrderCount;
}
