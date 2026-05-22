package com.lab.equipment_booking.service.impl;

import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.exception.BusinessException;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.UserService;
import com.lab.equipment_booking.utils.BCryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        try {
            return userMapper.findByUsername(username);
        } catch (Exception e) {
            logger.error("查询用户失败, username: {}", username, e);
            throw new BusinessException(500, "查询用户失败: " + e.getMessage());
        }
    }

    @Override
    public boolean register(User user) {
        try {
            User existingUser = userMapper.findByUsername(user.getUsername());
            if (existingUser != null) {
                throw new BusinessException(400, "用户名已存在");
            }
            user.setPassword(BCryptUtil.encode(user.getPassword()));
            user.setCreateTime(LocalDateTime.now());
            return userMapper.insert(user) > 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("注册用户失败, username: {}", user.getUsername(), e);
            throw new BusinessException(500, "注册失败: " + e.getMessage());
        }
    }
}