package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周度目标实体类
 * 对应数据库表：weekly_target
 * 按门店或员工维度设置周度业绩目标，颗粒度比月度目标更细，用于短期追踪。
 */
@Data
@TableName("weekly_target")
public class WeeklyTarget implements Serializable {

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

    /** 目标周（取当周周一日期） */
    private LocalDate targetWeek;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
