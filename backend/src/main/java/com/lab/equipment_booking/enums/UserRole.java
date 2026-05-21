package com.lab.equipment_booking.enums;

public enum UserRole {
    STUDENT(0, "学生"),
    ADMIN(1, "管理员");
    
    private final int code;
    private final String description;
    
    UserRole(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() { return code; }
    public String getDescription() { return description; }
    
    public static UserRole fromCode(Integer code) {
        if (code == null) return null;
        for (UserRole role : values()) {
            if (role.code == code) return role;
        }
        throw new IllegalArgumentException("无效的用户角色码: " + code);
    }
}