package com.pingpong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 DTO
 * 封装登录成功后返回给前端的信息：Token + 当前登录员工的基本信息。
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /** JWT Token，后续请求需放在 Authorization 请求头中 */
    private String token;

    /** 员工ID */
    private Long staffId;

    /** 员工姓名 */
    private String name;

    /** 所属门店ID */
    private Long storeId;

    /** 角色：boss / shop_owner / coach / sales */
    private String role;
}
