package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 门店排名条目 VO
 * 用于门店业绩排名、门店消课排名等场景。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreRankingItem {

    /** 门店ID */
    private Long storeId;

    /** 门店名称 */
    private String storeName;

    /** 销售额 */
    private BigDecimal salesAmount;

    /** 订单数 */
    private Long orderCount;

    /** 消课总课时 */
    private Long lessonsConsumed;

    /** 消课金额（按课时单价折算） */
    private BigDecimal lessonAmount;

    /** 消课次数 */
    private Long lessonCount;

    /** 排名名次（从1开始） */
    private Integer rank;
}
