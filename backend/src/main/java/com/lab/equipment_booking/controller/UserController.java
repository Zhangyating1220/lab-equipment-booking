package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.service.UserService;
import com.lab.equipment_booking.utils.BCryptUtil;
import com.lab.equipment_booking.utils.JwtUtil;
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
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        User user = userService.findByUsername(username);
        Map<String, Object> result = new HashMap<>();
        if (user != null && BCryptUtil.matches(password, user.getPassword())) {
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("token", token);
            result.put("role", user.getRole());
            result.put("name", user.getName());
        } else {
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
        }
        return result;
    }
}