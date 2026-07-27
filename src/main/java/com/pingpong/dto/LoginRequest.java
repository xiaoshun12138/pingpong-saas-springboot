package com.pingpong.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 * 封装登录接口的入参：手机号 + 密码。
 */
@Data
public class LoginRequest {

    /** 手机号（登录账号） */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 登录密码 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
