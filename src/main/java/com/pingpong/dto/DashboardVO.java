package com.pingpong.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 仪表盘总览数据 VO（View Object）
 * 封装首页看板展示的核心统计指标。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    /** 当前门店名称（boss为"总部"，店长为具体门店名） */
    private String storeName;

    /** 门店总数 */
    private long storeCount;

    /** 员工总数 */
    private long staffCount;

    /** 学员总数 */
    private long studentCount;

    /** 活跃学员数（status=1 在读） */
    private long activeStudentCount;

    /** 本月消课次数（记录条数） */
    private long monthConsumptionCount;

    /** 本月消课总课时数 */
    private long monthConsumptionLessons;

    /** 本月消课总金额 */
    private BigDecimal monthConsumptionAmount;

    /** 本月新报人数 */
    private long monthNewCount;

    /** 本月新报总金额 */
    private BigDecimal monthNewAmount;

    /** 本月续费人数 */
    private long monthRenewCount;

    /** 本月续费总金额 */
    private BigDecimal monthRenewAmount;

    /** 本月退款笔数 */
    private long monthRefundCount;

    /** 本月退款总金额 */
    private BigDecimal monthRefundAmount;
}
