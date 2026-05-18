package com.lab.equipment_booking.service;

import com.lab.equipment_booking.entity.User;

public interface UserService {
    User findByUsername(String username);
}