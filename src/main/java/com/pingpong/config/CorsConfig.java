package com.pingpong.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置类
 * 配置 CORS 跨域过滤器，允许前端（不同域名/端口）访问后端接口。
 * 开发环境全放行方便调试，生产环境通过环境变量 CORS_ALLOWED_ORIGINS 限制具体域名。
 */
@Configuration
public class CorsConfig {

    /**
     * 允许的前端域名，从配置文件读取。
     * 开发环境默认 "*"（全放行），生产环境通过环境变量 CORS_ALLOWED_ORIGINS 注入。
     * 多个域名用逗号分隔，例如：https://admin.pingpong.com,https://boss.pingpong.com
     */
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * 注册 CORS 过滤器 Bean
     *
     * @return 配置好的 CorsFilter 实例
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有 HTTP 方法（GET/POST/PUT/DELETE 等）
        config.addAllowedMethod("*");
        // 预检请求缓存时间，单位秒
        config.setMaxAge(3600L);

        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        for (String origin : origins) {
            String trimmed = origin.trim();
            if ("*".equals(trimmed)) {
                // 开发环境：通配模式，兼容带凭证的请求
                config.addAllowedOriginPattern("*");
            } else {
                // 生产环境：精确域名白名单
                config.addAllowedOrigin(trimmed);
            }
        }
        // 允许携带凭证（Cookie、Authorization 头）
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
