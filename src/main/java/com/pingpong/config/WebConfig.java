package com.pingpong.config;

import com.pingpong.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * Spring MVC 配置类
 * 负责注册自定义拦截器、配置静态资源等 Web 层相关设置。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /**
     * 注册拦截器
     * 将 AuthInterceptor 注册到 Spring MVC 拦截器链中：
     * - 拦截所有 /api/** 路径下的接口
     * - 排除登录接口 /api/auth/login（登录前还没有 Token）
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    /**
     * 静态资源配置：
     * - index.html 禁止缓存，确保每次请求都拿到最新版本（Vite 构建的 JS 文件名带 hash，index.html 更新后自动指向新文件）
     * - 带 hash 的资源文件（JS/CSS/图片）缓存 30 天
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // index.html 禁缓存
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noCache().mustRevalidate());

        // 根路径也映射到 index.html（SPA fallback）
        registry.addResourceHandler("/")
                .addResourceLocations("classpath:/static/index.html")
                .setCacheControl(CacheControl.noCache().mustRevalidate());

        // 带 hash 的静态资源长期缓存
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
    }
}
