package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 排行榜条目 VO
 * 通用的排名数据结构，用于教练消课排名、教练业绩排名、销售业绩排名等场景。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingItem {

    /** 员工ID */
    private Long staffId;

    /** 员工姓名 */
    private String staffName;

    /** 所属门店名称 */
    private String storeName;

    /** 排名数值（金额或课时数，具体含义由接口决定） */
    private BigDecimal value;

    /** 关联数量（消课次数 / 订单数等） */
    private Long count;

    /** 消课金额（用于课消排名按金额排序） */
    private BigDecimal lessonAmount;

    /** 排名名次（从1开始） */
    private Integer rank;

    /** 角色标签（教练/销售，用于业绩排名页面展示） */
    private String roleLabel;

    /**
     * 兼容旧调用：7参数构造器（lessonAmount 默认 ZERO）
     */
    public RankingItem(Long staffId, String staffName, String storeName, BigDecimal value, Long count, Integer rank, String roleLabel) {
        this(staffId, staffName, storeName, value, count, BigDecimal.ZERO, rank, roleLabel);
    }
}
