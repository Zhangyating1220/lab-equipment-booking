package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.dto.LoginRequest;
import com.lab.equipment_booking.dto.RegisterRequest;
import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.service.UserService;
import com.lab.equipment_booking.utils.BCryptUtil;
import com.lab.equipment_booking.utils.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        User user = userService.findByUsername(username);
        Map<String, Object> result = new HashMap<>();
        if (user == null) {
            result.put("code", 401);
            result.put("message", "用户名不存在");
        } else if (!BCryptUtil.matches(password, user.getPassword())) {
            result.put("code", 401);
            result.put("message", "密码错误");
        } else {
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("token", token);
            result.put("userId", user.getId().toString());  // 转为字符串避免JS精度丢失
            result.put("role", user.getRole());
            result.put("name", user.getName());
        }
        return result;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(request.getPassword());
            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setRole(request.getRole() != null ? request.getRole() : 0);
            
            boolean success = userService.register(user);
            if (success) {
                result.put("code", 200);
                result.put("message", "注册成功");
            } else {
                result.put("code", 500);
                result.put("message", "注册失败");
            }
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}