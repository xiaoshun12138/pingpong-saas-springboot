package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学员实体类
 * 对应数据库表：student
 * 记录学员基本信息及总剩余课时，消课和退款时会同步更新剩余课时。
 * version 字段用于乐观锁，防止并发消课/退款时总剩余课时扣错。
 */
@Data
@TableName("student")
public class Student implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属门店ID */
    @NotNull(message = "所属门店不能为空")
    private Long storeId;

    /** 带教教练ID（主要负责人/班主任） */
    private Long primaryCoachId;

    /** 学员姓名 */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /** 联系电话 */
    private String phone;

    /** 年龄 */
    private Integer age;

    /** 家庭住址 */
    private String address;

    /** 获客来源（如：转介绍、地推、抖音等） */
    private String source;

    /** 注册日期（首次订单创建时间） */
    private LocalDateTime registeredAt;

    /** 最近上课日期（最近一次消课时间） */
    private LocalDateTime lastLessonAt;

    /** 总剩余课时（所有有效订单剩余课时之和），消课/退款时与订单课时同步扣减 */
    private Integer totalRemainingLessons;

    /** 乐观锁版本号，消课/退款扣减总剩余课时时自增，防止并发扣错 */
    @Version
    private Integer version;

    /** 学员状态：1在读 0停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
