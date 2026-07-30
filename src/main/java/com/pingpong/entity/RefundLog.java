package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录实体类
 * 对应数据库表：refund_log
 * 记录学员退课退款操作，退款后订单状态置为 refunded，同时扣减学员总剩余课时。
 * 退款记录为只读性质，只增不删改。
 */
@Data
@TableName("refund_log")
public class RefundLog implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属门店ID，退款时从订单冗余写入，用于Dashboard按门店统计退款 */
    private Long storeId;

    /** 关联的课包订单ID */
    @NotNull(message = "关联订单不能为空")
    private Long courseOrderId;

    /** 学员ID */
    @NotNull(message = "学员不能为空")
    private Long studentId;

    /** 退款金额，由后端按比例自动计算（实付×退款课时/总课时），前端传入的值会被覆盖 */
    private BigDecimal refundAmount;

    /** 退回的课时数，必须大于0 */
    @NotNull(message = "退回课时不能为空")
    @Positive(message = "退回课时必须大于0")
    private Integer refundLessons;

    /** 退款原因 */
    private String reason;

    /** 操作人ID（谁执行的退款，后端从JWT token自动填充） */
    private Long operatorId;

    /** 创建时间（退款时间） */
    private LocalDateTime createdAt;

    /** 学员姓名（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String studentName;

    /** 订单编号（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String orderNo;

    /** 操作人姓名（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String operatorName;

    /** 门店名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String storeName;
}
