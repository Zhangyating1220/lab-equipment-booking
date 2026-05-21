package com.lab.equipment_booking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lab.equipment_booking.enums.BookingStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约实体类
 */
@Data
@TableName("booking")
public class Booking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;           // 预约人ID
    private Long equipmentId;      // 设备ID
    private LocalDateTime startTime; // 开始时间
    private LocalDateTime endTime;   // 结束时间
    private String reason;         // 预约事由
    private Integer status;        // 状态（使用 BookingStatus 枚举）
    private String rejectReason;   // 拒绝原因
    private Long approverId;       // 审批人ID（新增）
    private LocalDateTime approveTime; // 审批时间（新增）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 获取状态枚举
     */
    public BookingStatus getStatusEnum() {
        return BookingStatus.fromCode(status);
    }
}