package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.common.Result;
import com.lab.equipment_booking.entity.Reservation;
import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservation")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * 创建预约（包含冲突校验）
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> create(@Valid @RequestBody Reservation reservation) {
        // 检查用户是否存在
        Long userId = reservation.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("success", false);
            data.put("message", "用户不存在");
            return Result.success(data);
        }
        
        // 使用数据库中的真实用户 ID
        reservation.setUserId(user.getId());
        
        int result = reservationService.createReservation(reservation);
        Map<String, Object> data = new HashMap<>();
        if (result == 0) {
            data.put("success", true);
            data.put("message", "预约提交成功，等待审批");
            return Result.success(data);
        } else if (result == 1) {
            data.put("success", false);
            data.put("message", "该时段已被预约，请选择其他时间");
            return Result.success(data);
        } else {
            data.put("success", false);
            data.put("message", "设备不可用");
            return Result.success(data);
        }
    }

    /**
     * 检查时段冲突
     */
    @GetMapping("/check-conflict")
    public Result<Map<String, Object>> checkConflict(@RequestParam Long equipmentId,
                                                     @RequestParam String startTime,
                                                     @RequestParam String endTime) {
        boolean hasConflict = reservationService.hasConflict(equipmentId, startTime, endTime);
        Map<String, Object> data = new HashMap<>();
        data.put("hasConflict", hasConflict);
        data.put("message", hasConflict ? "该时段已被预约" : "该时段可用");
        return Result.success(data);
    }

    /**
     * 获取设备某天的可用时段（兼容前端 /booking 路径）
     */
    @GetMapping({"/available-slots", "/booking/available-slots"})
    public Result<List<String>> getAvailableSlots(
            @RequestParam Long equipmentId,
            @RequestParam String date) {
        return Result.success(reservationService.getAvailableSlots(equipmentId, date));
    }

    /**
     * 获取用户预约列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserReservations(@PathVariable Long userId) {
        return Result.success(reservationService.getUserReservations(userId));
    }

    /**
     * 获取待审批列表
     */
    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingReservations() {
        return Result.success(reservationService.getPendingReservations());
    }

    // 修改审批通过接口
@PutMapping("/approve/{id}")
public Result<Void> approve(@PathVariable Long id) {
    boolean success = reservationService.approveReservation(id);
    if (success) {
        return Result.success("审批通过", null);
    } else {
        return Result.error("审批失败：预约不存在或状态不是待审批"); // ✅ 增强错误提示
    }
}

// 修改拒绝预约接口
@PutMapping("/reject/{id}")
public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> request) {
    String reason = request.get("reason");
    if (reason == null || reason.trim().isEmpty()) {
        return Result.error("拒绝原因不能为空"); // ✅ 增强错误提示
    }
    boolean success = reservationService.rejectReservation(id, reason.trim());
    if (success) {
        return Result.success("已拒绝", null);
    } else {
        return Result.error("拒绝失败：预约不存在或状态不是待审批"); // ✅ 增强错误提示
    }
}

// 修改取消预约接口
@PutMapping("/cancel/{id}")
public Result<Void> cancel(@PathVariable Long id) {
    boolean success = reservationService.cancelReservation(id);
    if (success) {
        return Result.success("已取消", null);
    } else {
        return Result.error("取消失败：预约不存在或状态不允许取消"); // ✅ 增强错误提示
    }
}
}