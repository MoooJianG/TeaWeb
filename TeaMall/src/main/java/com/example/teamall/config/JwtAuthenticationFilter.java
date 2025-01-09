/**
 * JWT认证过滤器
 * 负责处理JWT token的验证和用户认证
 * 继承OncePerRequestFilter确保每个请求只执行一次过滤
 */
package com.example.teamall.config;

import com.example.teamall.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** JWT工具类 */
    @Autowired
    private JwtUtils jwtUtils;

    /** 用户详情服务 */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 执行过滤器逻辑
     * 1. 检查是否是公开接口
     * 2. 提取并验证JWT token
     * 3. 加载用户信息
     * 4. 设置认证信息
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        try {
            String path = request.getRequestURI();
            // 对于公开接口，不进行token验证
            if (path.contains("/api/users/login") || 
                path.contains("/api/users/register") ||
                path.contains("/api/products") ||
                path.contains("/api/categories")) {
                chain.doFilter(request, response);
                return;
            }

            // 获取Authorization头部
            final String authHeader = request.getHeader("Authorization");
            log.debug("Auth header: {}", authHeader);
            
            // 检查Authorization头部格式
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.debug("No Bearer token found");
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\"}");
                return;
            }

            // 提取并验证token
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtUtils.getUsernameFromToken(jwt);
            log.debug("Extracted email: {}", userEmail);

            // 验证用户并设置认证信息
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    // 创建认证token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    
                    // 设置认证详情
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    chain.doFilter(request, response);
                } else {
                    // token验证失败
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\":401,\"message\":\"token已过期\"}");
                }
            } else {
                // 无效的token
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"message\":\"无效的token\"}");
            }
        } catch (Exception e) {
            // 认证过程发生异常
            log.error("JWT认证失败", e);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"认证失败\"}");
        }
    }
} 