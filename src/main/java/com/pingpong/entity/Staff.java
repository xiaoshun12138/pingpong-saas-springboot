package com.pingpong.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体类
 * 对应数据库表：staff
 * 包含老板、店长、教练、销售四种角色，是登录鉴权和业务操作的主体。
 */
@Data
@TableName("staff")
public class Staff implements Serializable {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属门店ID */
    @NotNull(message = "所属门店不能为空")
    private Long storeId;

    /** 员工姓名 */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /** 手机号（登录账号） */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 角色：boss/ shop_owner/ coach/ sales */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(boss|shop_owner|coach|sales)$", message = "角色只能是 boss/shop_owner/coach/sales")
    private String role;

    /** 登录密码（BCrypt加密或明文兼容） */
    private String password;

    /** 入职日期 */
    private LocalDate entryDate;

    /** 账号状态：1启用 0禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0未删除 1已删除 */
    @TableLogic
    private Integer deleted;
}
