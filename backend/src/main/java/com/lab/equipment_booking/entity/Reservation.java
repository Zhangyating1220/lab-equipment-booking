package com.lab.equipment_booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    @JsonProperty("userId")
    private Long userId;
    
    @TableField("equipment_id")
    @JsonProperty("equipmentId")
    private Long equipmentId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @TableField("start_time")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @TableField("end_time")
    private LocalDateTime endTime;
    
    @TableField("purpose")
    private String purpose;
    
    @TableField("status")
    private Integer status;   // 0-待审批, 1-已通过, 2-已拒绝, 3-已取消
    
    @TableField("reject_reason")
    private String rejectReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @TableField("update_time")
    private LocalDateTime updateTime;
}