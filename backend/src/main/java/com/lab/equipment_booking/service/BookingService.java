package com.lab.equipment_booking.service;

import com.lab.equipment_booking.dto.BookingDTO;
import com.lab.equipment_booking.entity.Booking;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    
    /**
     * 创建预约（含冲突校验）
     * @param booking 预约对象
     * @return 是否创建成功
     */
    boolean createBooking(Booking booking);
    
    /**
     * 查询用户预约列表
     * @param userId 用户ID
     * @return 预约列表
     */
    List<Booking> getBookingsByUserId(Long userId);
    
    /**
     * 查询用户预约列表（含设备名称）
     * @param userId 用户ID
     * @return 预约列表
     */
    List<BookingDTO> getBookingsByUserIdWithEquipment(Long userId);
    
    /**
     * 查询待审批列表（管理员使用）
     * @return 待审批预约列表
     */
    List<Booking> getPendingApprovals();
    
    /**
     * 查询待审批列表（含用户和设备名称）
     * @return 待审批预约列表
     */
    List<BookingDTO> getPendingApprovalsWithNames();
    
    /**
     * 审批预约
     * @param bookingId 预约ID
     * @param approve 是否通过
     * @param rejectReason 拒绝原因（拒绝时必填）
     * @return 是否审批成功
     */
    boolean approveBooking(Long bookingId, boolean approve, String rejectReason);
    
    /**
     * 取消预约
     * @param bookingId 预约ID
     * @param userId 用户ID（用于权限校验）
     * @return 是否取消成功
     */
    boolean cancelBooking(Long bookingId, Long userId);
    
    /**
     * 获取可用时段
     * @param equipmentId 设备ID
     * @param date 日期（格式：yyyy-MM-dd）
     * @return 可用时段列表
     */
    List<String> getAvailableSlots(Long equipmentId, String date);
    
    /**
     * 查询已通过的预约（供使用记录模块调用）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 已通过预约列表
     */
    List<Booking> getApprovedBookings(LocalDateTime startTime, LocalDateTime endTime);
}