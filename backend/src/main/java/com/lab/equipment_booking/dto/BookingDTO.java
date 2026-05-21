package com.lab.equipment_booking.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约详情DTO（包含用户和设备名称）
 */
@Data
public class BookingDTO {
    private Long id;
    private Long userId;
    private String userName;      // 申请人姓名
    private Long equipmentId;
    private String equipmentName; // 设备名称
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;
}