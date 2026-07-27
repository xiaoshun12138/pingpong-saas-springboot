package com.pingpong.interceptor;

import com.pingpong.common.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权拦截器
 * 在请求进入 Controller 之前校验 Token 是否有效。
 * 校验通过后将员工信息（staffId、name、storeId、role）存入 request attribute，
 * 供后续 Controller/Service 层直接取用，实现数据权限隔离。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * 请求前置处理：校验 Token 有效性
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }
        try {
            var claims = JwtUtil.parse(token.substring(7));
            request.setAttribute("staffId", Long.valueOf(claims.getSubject()));
            request.setAttribute("name", claims.get("name", String.class));
            request.setAttribute("storeId", claims.get("storeId", Long.class));
            request.setAttribute("role", claims.get("role", String.class));
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }
    }
}
