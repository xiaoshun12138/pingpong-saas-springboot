package com.pingpong.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 所有 Controller 接口的返回值都用 R<T> 包裹，保证前后端交互格式统一。
 *
 * @param <T> 业务数据的泛型类型
 */
@Data
public class R<T> implements Serializable {

    /** 响应状态码：200成功，400参数错误，401未登录，403无权限，500服务器异常 */
    private int code;

    /** 响应提示信息 */
    private String message;

    /** 响应业务数据 */
    private T data;

    /** 无参构造方法 */
    public R() {}

    /**
     * 全参构造方法
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    业务数据
     */
    public R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 操作成功（无数据）
     *
     * @return 成功响应，data 为 null
     */
    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null);
    }

    /**
     * 操作成功（带数据）
     *
     * @param data 返回的业务数据
     * @return 成功响应，包含业务数据
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data);
    }

    /**
     * 操作成功（自定义消息 + 数据）
     *
     * @param message 自定义提示信息
     * @param data    返回的业务数据
     * @return 成功响应
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }

    /**
     * 操作失败（默认 500）
     *
     * @return 失败响应
     */
    public static <T> R<T> fail() {
        return new R<>(500, "操作失败", null);
    }

    /**
     * 操作失败（自定义消息）
     *
     * @param message 失败提示信息
     * @return 失败响应
     */
    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null);
    }

    /**
     * 操作失败（自定义状态码 + 消息）
     *
     * @param code    错误状态码
     * @param message 错误提示信息
     * @return 失败响应
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}
