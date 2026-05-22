package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.common.Result;
import com.lab.equipment_booking.dto.ReservationRequest;
import com.lab.equipment_booking.entity.Reservation;
import com.lab.equipment_booking.service.ReservationService;
import com.lab.equipment_booking.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingController {
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取设备某天的可用时段（直接返回字符串数组）
     */
    @GetMapping("/available-slots")
    public List<String> getAvailableSlots(
            @RequestParam Long equipmentId,
            @RequestParam String date) {
        return reservationService.getAvailableSlots(equipmentId, date);
    }

    /**
     * 创建预约（包含冲突校验，兼容 /submit 路径）
     */
    @PostMapping({"/create", "/submit"})
    public Result<Map<String, Object>> create(@Valid @RequestBody ReservationRequest request, HttpServletRequest httpRequest) {
        // 从请求头中获取 token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> data = new HashMap<>();
            data.put("success", false);
            data.put("message", "未登录或 token 无效");
            return Result.success(data);
        }
        
        String token = authHeader.substring(7);
        Long userId = null;
        try {
            Claims claims = jwtUtil.parseToken(token);
            userId = claims.get("userId", Long.class);
            logger.info("从 token 解析出 userId: {}", userId);
        } catch (Exception e) {
            logger.error("解析 token 失败: {}", e.getMessage());
            Map<String, Object> data = new HashMap<>();
            data.put("success", false);
            data.put("message", "token 解析失败: " + e.getMessage());
            return Result.success(data);
        }
        
        if (userId == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("success", false);
            data.put("message", "无法获取用户信息");
            return Result.success(data);
        }
        
        // 转换为实体
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setEquipmentId(request.getEquipmentId());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPurpose(request.getPurpose());

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

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> getMyBookings(HttpServletRequest httpRequest, @RequestParam(required = false) String userId) {
        String effectiveUserId = userId;
        
        // 如果没有提供 userId 参数，则从 token 中解析
        if (effectiveUserId == null || effectiveUserId.isEmpty()) {
            // 从请求头中获取 token
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("未登录或 token 无效");
            }
            
            String token = authHeader.substring(7);
            Long parsedUserId = null;
            try {
                Claims claims = jwtUtil.parseToken(token);
                parsedUserId = claims.get("userId", Long.class);
            } catch (Exception e) {
                return Result.error("token 解析失败: " + e.getMessage());
            }
            
            if (parsedUserId == null) {
                return Result.error("无法获取用户信息");
            }
            
            effectiveUserId = parsedUserId.toString();
        }
        
        // 将字符串 userId 转换为 Long 类型用于数据库查询
        Long userIdLong = null;
        try {
            userIdLong = Long.parseLong(effectiveUserId);
        } catch (NumberFormatException e) {
            return Result.error("无效的用户ID: " + effectiveUserId);
        }
        
        // 调用服务层获取用户预约列表
        List<Map<String, Object>> reservations = reservationService.getUserReservations(userIdLong);
        return Result.success(reservations);
    }

    @PostMapping("/cancel/{id}")
    public Result<Void> cancelBooking(@PathVariable Long id, HttpServletRequest httpRequest) {
        // 从请求头中获取 token
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未登录或 token 无效");
        }
        
        String token = authHeader.substring(7);
        Long userId = null;
        try {
            Claims claims = jwtUtil.parseToken(token);
            userId = claims.get("userId", Long.class);
        } catch (Exception e) {
            return Result.error("token 解析失败: " + e.getMessage());
        }
        
        if (userId == null) {
            return Result.error("无法获取用户信息");
        }
        
        boolean success = reservationService.cancelReservation(id);
        if (success) {
            return Result.success("取消成功", null);
        } else {
            return Result.error("取消失败：预约不存在或状态不允许取消");
        }
    }
}