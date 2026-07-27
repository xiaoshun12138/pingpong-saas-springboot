package com.pingpong;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 乒乓球培训 SaaS 管理系统 —— Spring Boot 启动类
 * 通过 @MapperScan 扫描 mapper 包下的所有 Mapper 接口，自动注册为 Bean。
 */
@SpringBootApplication
@MapperScan("com.pingpong.mapper")
public class PingPongApplication {

    /**
     * 应用程序入口方法
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PingPongApplication.class, args);
    }
}
