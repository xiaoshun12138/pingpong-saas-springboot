package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 课包订单实体类
 * 对应数据库表：course_order
 * 学员购买课包时生成的订单，记录课时消耗进度。
 * version 字段用于乐观锁，防止并发消课时出现超扣问题。
 */
@Data
@TableName("course_order")
public class CourseOrder implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单编号（业务唯一标识，新增时留空则自动生成） */
    private String orderNo;

    /** 类型：new-新报 renew-续费 */
    private String type;

    /** 所属门店ID */
    @NotNull(message = "所属门店不能为空")
    private Long storeId;

    /** 学员ID */
    @NotNull(message = "学员不能为空")
    private Long studentId;

    /** 销售员工ID（谁签的单） */
    private Long salesId;

    /** 负责教练ID */
    private Long coachId;

    /** 课包类型ID */
    @NotNull(message = "课包类型不能为空")
    private Long courseTypeId;

    /** 实付金额 */
    @NotNull(message = "实付金额不能为空")
    private BigDecimal paidAmount;

    /** 订单总课时，新增时如未传则从课包类型自动复制 */
    private Integer totalLessons;

    /** 剩余课时，新增时自动=总课时，后续由消课/退款接口维护，不可通过 PUT 直接改 */
    private Integer remainingLessons;

    /** 已消课时，由消课接口自动累加，不可通过 PUT 直接改 */
    private Integer consumedLessons;

    /** 乐观锁版本号，消课/退款扣减课时或修改状态时自动 +1 */
    @Version
    private Integer version;

    /** 订单来源 */
    private String source;

    /** 备注 */
    private String remark;

    /** 订单状态：active进行中 / refunded已退款 */
    @NotBlank(message = "订单状态不能为空")
    private String status;

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

    /** 课包名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String courseTypeName;

    /** 门店名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String storeName;
    /** 扩展参数（前端传 studentName / salesName，不落库） */
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
