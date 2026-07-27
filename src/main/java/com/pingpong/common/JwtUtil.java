package com.pingpong.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 负责生成、解析、校验 Token，用于登录鉴权。
 * Token 中携带员工ID、姓名、门店ID、角色信息，拦截器解析后存入请求上下文。
 * 密钥从 application.yml 读取，支持环境变量 JWT_SECRET 注入（生产环境必须注入）。
 */
@Component
public class JwtUtil {

    /** JWT 签名密钥，从配置文件读取，生产环境通过环境变量 JWT_SECRET 注入 */
    private static String SECRET;

    /** Token 有效期：24小时（毫秒） */
    private static final long EXPIRE_MS = 24 * 60 * 60 * 1000L;

    /**
     * 从配置文件注入 JWT 密钥
     */
    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtil.SECRET = secret;
    }

    /**
     * 获取签名密钥对象（每次调用时基于当前 SECRET 生成）
     */
    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param staffId 员工ID（作为 subject）
     * @param name    员工姓名
     * @param storeId 所属门店ID
     * @param role    角色（boss/shop_owner/coach/sales）
     * @return 签好名的 JWT 字符串
     */
    public static String generate(Long staffId, String name, Long storeId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(staffId))
                .claim("name", name)
                .claim("storeId", storeId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRE_MS))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 JWT Token，获取载荷（Claims）
     *
     * @param token JWT 字符串
     * @return 载荷对象，包含 subject、自定义 claim、签发/过期时间等
     * @throws io.jsonwebtoken.JwtException 签名无效或格式错误时抛出
     */
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT 字符串
     * @return true 已过期或解析失败；false 有效
     */
    public static boolean isExpired(String token) {
        try {
            return parse(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
