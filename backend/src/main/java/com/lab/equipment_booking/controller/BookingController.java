<<<<<<< HEAD
package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.common.Result;
import com.lab.equipment_booking.constants.DateTimeConstants;
import com.lab.equipment_booking.dto.BookingDTO;
import com.lab.equipment_booking.entity.Booking;
import com.lab.equipment_booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitBooking(@RequestBody Map<String, Object> request) {
        // 参数校验
        if (!request.containsKey("userId") || !request.containsKey("equipmentId") ||
            !request.containsKey("startTime") || !request.containsKey("endTime")) {
            return Result.error(400, "缺少必填参数");
        }
        
        Booking booking = new Booking();
        booking.setUserId(Long.parseLong(request.get("userId").toString()));
        booking.setEquipmentId(Long.parseLong(request.get("equipmentId").toString()));
        booking.setStartTime(LocalDateTime.parse(request.get("startTime").toString(), DateTimeConstants.DEFAULT_FORMATTER));
        booking.setEndTime(LocalDateTime.parse(request.get("endTime").toString(), DateTimeConstants.DEFAULT_FORMATTER));
        booking.setReason((String) request.get("reason"));
        
        boolean success = bookingService.createBooking(booking);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("bookingId", booking.getId());
        
        return success ? Result.success("预约成功，等待审批", result) : Result.error("预约失败");
    }
    
    @GetMapping("/my")
    public Result<List<BookingDTO>> getMyBookings(@RequestParam Long userId) {
        List<BookingDTO> bookings = bookingService.getBookingsByUserIdWithEquipment(userId);
        return Result.success(bookings);
    }
    
    @GetMapping("/pending")
    public Result<List<BookingDTO>> getPendingApprovals() {
        List<BookingDTO> bookings = bookingService.getPendingApprovalsWithNames();
        return Result.success(bookings);
    }
    
    @PostMapping("/approve/{id}")
    public Result<Map<String, Object>> approveBooking(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        // 参数校验
        if (!request.containsKey("approve")) {
            return Result.error(400, "缺少审批参数");
        }
        
        // 安全的类型转换（处理前端可能传递的不同类型）
        Object approveObj = request.get("approve");
        Boolean approve = null;
        if (approveObj instanceof Boolean) {
            approve = (Boolean) approveObj;
        } else if (approveObj instanceof String) {
            approve = Boolean.parseBoolean((String) approveObj);
        }
        
        // 检查 approve 是否为 null
        if (approve == null) {
            return Result.error(400, "审批参数格式错误");
        }
        
        String rejectReason = (String) request.get("rejectReason");
        
        // 如果拒绝，检查是否有拒绝原因
        if (!approve && (rejectReason == null || rejectReason.trim().isEmpty())) {
            return Result.error(400, "拒绝预约需要填写拒绝原因");
        }
        
        boolean success = bookingService.approveBooking(id, approve, rejectReason);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", approve ? "审批通过" : "已拒绝");
        
        return success ? Result.success(result) : Result.error("审批失败");
    }
    
    @PostMapping("/cancel/{id}")
    public Result<Map<String, Object>> cancelBooking(
            @PathVariable Long id,
            @RequestParam Long userId) {
        boolean success = bookingService.cancelBooking(id, userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        
        return success ? Result.success("取消成功", result) : Result.error("取消失败");
    }
    
    @GetMapping("/available-slots")
    public Result<List<String>> getAvailableSlots(
            @RequestParam Long equipmentId,
            @RequestParam String date) {
        List<String> slots = bookingService.getAvailableSlots(equipmentId, date);
        return Result.success(slots);
    }
=======
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
        reservation.setUserId(userId);  // 从 token 解析的 userId
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
>>>>>>> origin/main
}