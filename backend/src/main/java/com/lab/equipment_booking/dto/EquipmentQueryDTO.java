package com.lab.equipment_booking.dto;

import lombok.Data;

@Data
public class EquipmentQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String category;   // 设备类别
    private Integer status;    // 状态
    private String name;       // 设备名称（模糊查询）
}