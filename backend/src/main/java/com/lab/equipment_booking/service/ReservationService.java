package com.lab.equipment_booking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lab.equipment_booking.entity.Reservation;
import java.util.List;
import java.util.Map;

public interface ReservationService extends IService<Reservation> {

    /**
     * 创建预约（包含冲突校验）
     * @return 0-成功, 1-时段冲突, 2-设备不可用
     */
    int createReservation(Reservation reservation);

    /**
     * 检查时段是否冲突
     */
    boolean hasConflict(Long equipmentId, String startTime, String endTime);

    /**
     * 获取用户的预约列表（包含设备信息）
     */
    List<Map<String, Object>> getUserReservations(Long userId);

    /**
     * 获取待审批的预约列表（包含设备信息）
     */
    List<Map<String, Object>> getPendingReservations();

    /**
     * 获取设备某天的可用时段
     */
    List<String> getAvailableSlots(Long equipmentId, String date);

    /**
     * 审批通过预约
     */
    boolean approveReservation(Long id);

    /**
     * 拒绝预约（需填写原因）
     */
    boolean rejectReservation(Long id, String reason);

    /**
     * 取消预约
     */
    boolean cancelReservation(Long id);
}