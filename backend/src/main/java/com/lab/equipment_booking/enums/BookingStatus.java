package com.lab.equipment_booking.enums;

public enum BookingStatus {
    PENDING(0, "待审批"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    CANCELLED(3, "已取消");
    
    private final int code;
    private final String description;
    
    BookingStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() { return code; }
    public String getDescription() { return description; }
    
    public static BookingStatus fromCode(Integer code) {
        if (code == null) return null;
        for (BookingStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("无效的预约状态码: " + code);
    }
}