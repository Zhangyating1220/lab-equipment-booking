package com.lab.equipment_booking.service.impl;

import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
}