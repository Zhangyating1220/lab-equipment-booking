package com.lab.equipment_booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EquipmentQueryDTO {
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;
    
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 10;
    
    @Size(max = 50, message = "类别名称不能超过50个字符")
    private String category;   // 设备类别
    
    @Min(value = 0, message = "状态值无效")
    @Max(value = 2, message = "状态值无效")
    private Integer status;    // 状态
    
    @Size(max = 100, message = "设备名称不能超过100个字符")
    private String name;       // 设备名称（模糊查询）
}