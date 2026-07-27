package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 排课记录实体类
 * 对应数据库表：lesson_schedule
 * 记录教练与学员的课程预约安排，支持按日期、按教练查询排课表。
 */
@Data
@TableName("lesson_schedule")
public class LessonSchedule implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属门店ID */
    @NotNull(message = "门店不能为空")
    private Long storeId;

    /** 教练ID */
    @NotNull(message = "教练不能为空")
    private Long coachId;

    /** 学员ID */
    @NotNull(message = "学员不能为空")
    private Long studentId;

    /** 关联的课包订单ID（可选） */
    private Long courseOrderId;

    /** 排课日期 */
    private LocalDate scheduleDate;

    /** 上课开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    /** 上课结束时间 */
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    /** 本节课教学内容 */
    private String lessonContent;

    /** 备注 */
    private String remark;

    /** 排课状态：scheduled已排课 / completed已完成 / cancelled已取消 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
