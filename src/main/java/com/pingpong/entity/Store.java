package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 门店实体类
 * 对应数据库表：store
 * SaaS 多门店架构的基础单位，员工、学员、订单等都绑定到具体门店。
 */
@Data
@TableName("store")
public class Store implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 门店名称 */
    @NotBlank(message = "门店名称不能为空")
    private String name;

    /** 门店地址 */
    private String address;

    /** 联系电话 */
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    /** 门店状态：1营业中 0已关闭 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
