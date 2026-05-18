package com.lab.equipment_booking.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String name;
    private Integer role;  // 0学生 1管理员
    private String phone;
    private LocalDateTime createTime;
}