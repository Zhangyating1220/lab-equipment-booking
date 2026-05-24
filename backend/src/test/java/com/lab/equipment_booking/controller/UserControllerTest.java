package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.UserService;
import com.lab.equipment_booking.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testLogin_Success() throws Exception {
        User user = new User();
        user.setUsername("logintest001");
        user.setPassword("password123");
        user.setName("登录测试");
        user.setRole(0);
        userService.register(user);

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"logintest001\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value(0));
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nonexistent\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名不存在"));
    }

    @Test
    void testLogin_WrongPassword() throws Exception {
        User user = new User();
        user.setUsername("wrongpw001");
        user.setPassword("correctpassword");
        user.setName("密码错误测试");
        user.setRole(0);
        userService.register(user);

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"wrongpw001\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    @Test
    void testRegister_Success() throws Exception {
        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"regtest001\",\"password\":\"password123\",\"name\":\"注册测试\",\"phone\":\"13800138000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"));
    }

    @Test
    void testRegister_DuplicateUsername() throws Exception {
        User user = new User();
        user.setUsername("dup_reg_001");
        user.setPassword("password123");
        user.setName("重复注册测试");
        user.setRole(0);
        userService.register(user);

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"dup_reg_001\",\"password\":\"password456\",\"name\":\"重复注册测试2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
