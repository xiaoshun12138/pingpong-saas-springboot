package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课包类型实体类
 * 对应数据库表：course_type
 * 定义可售卖的课程套餐，如"10课时入门班"、"50课时年卡"等，是下单时的商品模板。
 */
@Data
@TableName("course_type")
public class CourseType implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课包名称 */
    @NotBlank(message = "课包名称不能为空")
    private String name;

    /** 课包包含的总课时数 */
    @NotNull(message = "总课时不能为空")
    private Integer totalLessons;

    /** 标价（建议售价） */
    @NotNull(message = "标价不能为空")
    private BigDecimal listPrice;

    /** 课包状态：1上架 0下架 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
