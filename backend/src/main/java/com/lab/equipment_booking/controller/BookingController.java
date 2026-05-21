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
}