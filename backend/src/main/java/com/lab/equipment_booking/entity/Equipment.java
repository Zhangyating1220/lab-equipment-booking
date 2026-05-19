package com.lab.equipment_booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String model;
    private String location;
    private Integer status;   // 0-可用, 1-维修中, 2-已废弃
    private String description;
    private LocalDateTime createTime;
}