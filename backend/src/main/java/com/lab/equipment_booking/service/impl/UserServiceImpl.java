package com.lab.equipment_booking.service.impl;

import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.exception.BusinessException;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}