package com.lab.equipment_booking.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class EquipmentQueryDTO {
    
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;
    
    @Min(value = 1, message = "每页大小最小为1")
    private Integer pageSize = 10;
    
    private String name;
    
    private String category;
    
    private Integer status;
}