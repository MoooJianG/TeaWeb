/**
 * JWT工具类
 * 提供JWT令牌的生成、解析和验证功能
 * 用于实现基于令牌的用户认证
 */
package com.example.teamall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具组件
 * 处理JWT令牌的生成、解析和验证
 * 使用HMAC-SHA256算法进行签名
 */
@Component
public class JwtUtils {

    /** JWT密钥，从配置文件中读取 */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT过期时间（毫秒），从配置文件中读取 */
    @Value("${jwt.expiration}")
    private Long expiration;

    /** 用于签名的密钥对象 */
    private SecretKey key;

    /**
     * 初始化密钥
     * 在构造函数之后自动调用
     * 将配置的密钥字符串转换为SecretKey对象
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT令牌
     * 将用户信息编码到令牌中，并设置过期时间
     * @param userDetails Spring Security的用户详情对象
     * @return JWT令牌字符串
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("created", new Date());
        
        return Jwts.builder()
            .setClaims(claims)
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 从令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 验证令牌是否有效
     * 检查令牌中的用户名是否匹配，以及令牌是否过期
     * @param token JWT令牌
     * @param userDetails Spring Security的用户详情对象
     * @return true表示有效，false表示无效
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中获取声明信息
     * @param token JWT令牌
     * @return 声明对象
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * 检查令牌是否过期
     * @param token JWT令牌
     * @return true表示已过期，false表示未过期
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }
} 