package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 目标仪表盘数据 VO
 * 用于业绩目标和课消目标的综合统计看板。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetDashboardVO {

    /** 年度目标 */
    private BigDecimal yearTarget;

    /** 年度实际完成 */
    private BigDecimal yearActual;

    /** 月度目标 */
    private BigDecimal monthTarget;

    /** 月度实际完成 */
    private BigDecimal monthActual;

    /** 周度目标 */
    private BigDecimal weekTarget;

    /** 周度实际完成 */
    private BigDecimal weekActual;

    /** 各月走势数据（12个月） */
    private List<BigDecimal> monthlyTrend;

    /** 各门店目标与完成对比 */
    private List<StoreComparisonItem> storeComparison;

    /** 各门店每月数据（表格用） */
    private List<StoreMonthlyItem> storeMonthlyData;

    /**
     * 门店对比项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreComparisonItem {
        private Long storeId;
        private String storeName;
        private BigDecimal target;
        private BigDecimal actual;
    }

    /**
     * 门店每月数据项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreMonthlyItem {
        private Long storeId;
        private String storeName;
        private Map<Integer, BigDecimal> monthData;
        private BigDecimal total;
    }
}
