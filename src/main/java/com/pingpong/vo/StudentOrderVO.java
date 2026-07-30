package com.pingpong.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 学员名下订单 VO
 * 用于学员详情页展示学员的课包列表。
 */
@Data
public class StudentOrderVO implements Serializable {

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 课包类型名称 */
    private String courseTypeName;

    /** 上课教练姓名 */
    private String coachName;

    /** 课包总课时 */
    private Integer totalLessons;

    /** 剩余课时 */
    private Integer remainingLessons;

    /** 实付金额 */
    private BigDecimal paidAmount;

    /** 订单状态（active/refunded/finished 等） */
    private String status;

    /** 已消课时 */
    private Integer consumedLessons;
}
