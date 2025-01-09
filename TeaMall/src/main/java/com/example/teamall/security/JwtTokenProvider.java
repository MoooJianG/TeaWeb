/**
 * JWT令牌提供者
 * 负责JWT令牌的生成、解析和验证
 * 用于实现基于令牌的身份验证
 */
package com.example.teamall.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    /** JWT密钥，从配置文件中读取 */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /** JWT过期时间（秒），从配置文件中读取 */
    @Value("${jwt.expiration}")
    private int jwtExpiration;

    /**
     * 生成JWT令牌
     * 使用用户认证信息创建一个新的JWT令牌
     * @param authentication 用户认证信息
     * @return JWT令牌字符串
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration * 1000);

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())  // 设置JWT主题为用户名
                .setIssuedAt(now)                         // 设置JWT签发时间
                .setExpiration(expiryDate)                // 设置JWT过期时间
                .signWith(SignatureAlgorithm.HS512, jwtSecret)  // 使用HS512算法和密钥签名
                .compact();
    }

    /**
     * 从JWT令牌中提取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    /**
     * 验证JWT令牌的有效性
     * 检查令牌的签名、格式、过期时间等
     * @param authToken JWT令牌
     * @return true表示令牌有效，false表示令牌无效
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            // 无效的JWT签名
            return false;
        } catch (MalformedJwtException ex) {
            // 无效的JWT令牌
            return false;
        } catch (ExpiredJwtException ex) {
            // 过期的JWT令牌
            return false;
        } catch (UnsupportedJwtException ex) {
            // 不支持的JWT令牌
            return false;
        } catch (IllegalArgumentException ex) {
            // JWT声明字符串为空
            return false;
        }
    }
} 