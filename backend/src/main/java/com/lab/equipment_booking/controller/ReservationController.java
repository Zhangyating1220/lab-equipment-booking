package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.common.Result;
import com.lab.equipment_booking.entity.Reservation;
import com.lab.equipment_booking.service.ReservationService;
import com.lab.equipment_booking.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private JwtUtil jwtUtil;

    /**
     * 创建预约（包含冲突校验，兼容 /booking/submit 路径）
     */
    @PostMapping({"/create", "/booking/submit"})
    public Result<Map<String, Object>> create(@Valid @RequestBody Reservation reservation) {
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
    public Result<List<Map<String, Object>>> getUserReservations(@PathVariable String userId) {
        return Result.success(reservationService.getUserReservations(userId));
    }

    /**
     * 获取待审批列表
     */
    @GetMapping("/pending")
    public Result<List<Map<String, Object>>> getPendingReservations() {
        return Result.success(reservationService.getPendingReservations());
    }

    /**
     * 审批通过预约（仅管理员可操作）
     */
    @PutMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, 
                                @RequestHeader(value = "Authorization", required = false) String token) {
        // ✅ 校验管理员权限
        if (!checkAdminPermission(token)) {
            return Result.error("无权限操作，仅管理员可审批");
        }
        
        boolean success = reservationService.approveReservation(id);
        if (success) {
            return Result.success("审批通过", null);
        } else {
            return Result.error("审批失败：预约不存在或状态不是待审批");
        }
    }

/**
     * 拒绝预约（仅管理员可操作）
     */
    @PutMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id, 
                               @RequestBody Map<String, String> request,
                               @RequestHeader(value = "Authorization", required = false) String token) {
        // ✅ 校验管理员权限
        if (!checkAdminPermission(token)) {
            return Result.error("无权限操作，仅管理员可审批");
        }
        
        String reason = request.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("拒绝原因不能为空");
        }
        boolean success = reservationService.rejectReservation(id, reason.trim());
        if (success) {
            return Result.success("已拒绝", null);
        } else {
            return Result.error("拒绝失败：预约不存在或状态不是待审批");
        }
    }

    /**
     * 检查管理员权限
     * @param token JWT令牌
     * @return 是否为管理员
     */
    private boolean checkAdminPermission(String token) {
        try {
            // 移除 Bearer 前缀
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Claims claims = jwtUtil.parseToken(token);
            Integer role = claims.get("role", Integer.class);
            
            // 角色 1 表示管理员
            return role != null && role == 1;
        } catch (Exception e) {
            return false;
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