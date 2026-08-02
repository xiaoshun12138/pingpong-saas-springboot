package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 消课记录实体类
 * 对应数据库表：course_consumption
 * 记录每一次课时消耗操作，关联具体订单、学员和教练。
 * 新增消课时会同步扣减订单剩余课时和学员总剩余课时（事务保证）。
 */
@Data
@TableName("course_consumption")
public class CourseConsumption implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属门店ID，消课时从订单自动填写，前端可不传 */
    private Long storeId;

    /** 学员ID */
    @NotNull(message = "学员不能为空")
    private Long studentId;

    /** 教练ID（谁上的课） */
    @NotNull(message = "教练不能为空")
    private Long coachId;

    /** 关联的课包订单ID */
    @NotNull(message = "关联订单不能为空")
    private Long courseOrderId;

    /** 关联的排课记录ID（从排课页面消课时自动填入，消课记录页面直接消课则为 null） */
    private Long scheduleId;

    /** 本次消耗的课时数，必须大于0 */
    @NotNull(message = "消课课时不能为空")
    @Positive(message = "消课课时必须大于0")
    private Integer lessons;

    /** 消课备注 */
    private String remark;

    /** 消课日期 */
    private LocalDate recordDate;

    /** 消课具体时间 */
    private LocalTime recordTime;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;

    /** 学员姓名（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String studentName;

    /** 教练姓名（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String coachName;

    /** 订单编号（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String orderNo;

    /** 门店名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String storeName;
}
