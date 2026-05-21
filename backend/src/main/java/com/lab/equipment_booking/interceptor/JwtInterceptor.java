package com.lab.equipment_booking.interceptor;

import com.lab.equipment_booking.common.Result;
import com.lab.equipment_booking.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // 公开路径直接放行
        if (isPublicPath(requestURI)) {
            return true;
        }
        
        // 获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        
        if (token == null || token.isEmpty()) {
            responseError(response, 401, "未登录，请先登录");
            return false;
        }
        
        // 去除Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            Claims claims = jwtUtil.parseToken(token);
            request.setAttribute("userId", claims.get("userId", Long.class));
            request.setAttribute("username", claims.get("username", String.class));
            request.setAttribute("role", claims.get("role", Integer.class));
            return true;
            
        } catch (ExpiredJwtException e) {
            responseError(response, 401, "登录已过期，请重新登录");
        } catch (Exception e) {
            responseError(response, 401, "Token无效");
        }
        
        return false;
    }
    
    private boolean isPublicPath(String requestURI) {
        String[] publicPaths = {
            "/api/user/login",
            "/api/user/register",
            "/api/equipment/list",
            "/api/equipment/info/"
        };
        for (String path : publicPaths) {
            if (requestURI.startsWith(path)) return true;
        }
        return false;
    }
    
    private void responseError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(code, message);
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
        writer.close();
    }
}