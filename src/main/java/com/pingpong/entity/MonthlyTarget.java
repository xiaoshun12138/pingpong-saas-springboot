package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 月度目标实体类
 * 对应数据库表：monthly_target
 * 按门店或员工维度设置月度业绩目标，支持金额目标和数量目标两种类型。
 */
@Data
@TableName("monthly_target")
public class MonthlyTarget implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属门店ID */
    private Long storeId;

    /** 目标所属员工ID（为空则表示门店整体目标） */
    private Long staffId;

    /** 目标类型：sales销售额 / newStudent新学员 / consumption消课 等 */
    private String targetType;

    /** 目标金额 */
    private BigDecimal targetAmount;

    /** 目标数量（如目标订单数、目标消课时数） */
    private Integer targetCount;

    /** 目标月份（取当月1号） */
    private LocalDate targetMonth;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
