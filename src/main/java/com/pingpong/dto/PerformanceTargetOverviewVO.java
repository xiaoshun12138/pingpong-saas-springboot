package com.pingpong.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 业绩目标总览 VO
 * 封装业绩目标看板所需的所有数据：年度/月度/周度目标完成率、走势、门店对比等。
 */
@Data
public class PerformanceTargetOverviewVO {

    /** 年度目标 */
    private BigDecimal yearTarget;
    /** 年度已完成 */
    private BigDecimal yearCompleted;
    /** 年度完成率 (0-1) */
    private BigDecimal yearRate;

    /** 月度目标 */
    private BigDecimal monthTarget;
    /** 月度已完成 */
    private BigDecimal monthCompleted;
    /** 月度完成率 (0-1) */
    private BigDecimal monthRate;

    /** 周度目标 */
    private BigDecimal weekTarget;
    /** 周度已完成 */
    private BigDecimal weekCompleted;
    /** 周度完成率 (0-1) */
    private BigDecimal weekRate;

    /** 公司各月业绩走势 [{month:'2026-01', amount:280000}, ...] */
    private List<Map<String, Object>> monthlyTrend;

    /** 各门店月度目标 vs 完成 [{storeId:1, storeName:'玄武分馆', target:50000, completed:34000}, ...] */
    private List<Map<String, Object>> storeTargets;

    /** 各门店月度业绩走势表格数据 */
    private List<StoreMonthlyGridItem> storeMonthlyGrid;

    @Data
    public static class StoreMonthlyGridItem {
        private Long storeId;
        private String storeName;
        /** 12 个月的业绩金额 */
        private List<BigDecimal> monthlyAmounts;
        /** 累计金额 */
        private BigDecimal total;
        /** 环比上月 (-1 表示无上月数据) */
        private BigDecimal mom;
    }
}
