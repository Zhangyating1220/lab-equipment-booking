package com.lab.equipment_booking.service.impl;

import com.lab.equipment_booking.dto.BookingDTO;
import com.lab.equipment_booking.entity.Booking;
import com.lab.equipment_booking.enums.BookingStatus;
import com.lab.equipment_booking.enums.UserRole;
import com.lab.equipment_booking.exception.BusinessException;
import com.lab.equipment_booking.mapper.BookingMapper;
import com.lab.equipment_booking.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service  // 标注为 Spring 服务组件
public class BookingServiceImpl implements BookingService {
    
    // 日志记录器（用于记录业务操作日志）
    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);
    
    // 自动注入 Mapper（依赖注入）
    @Autowired
    private BookingMapper bookingMapper;
    
    // 可用时段配置（可提取到配置文件中）
    private static final String[] TIME_SLOTS = {
        "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
        "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"
    };
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (Long) request.getAttribute("userId");
        }
        return null;
    }
    
    /**
     * 获取当前登录用户角色
     */
    private Integer getCurrentUserRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (Integer) request.getAttribute("role");
        }
        return null;
    }
    
    /**
     * 校验是否为管理员
     */
    private void checkAdminPermission() {
        Integer role = getCurrentUserRole();
        if (role == null || role != UserRole.ADMIN.getCode()) {
            throw new BusinessException(403, "无管理员权限");
        }
    }
    
    // ==================== 创建预约 ====================
    @Override
    public boolean createBooking(Booking booking) {
        // 参数校验
        if (booking.getUserId() == null || booking.getEquipmentId() == null) {
            throw new IllegalArgumentException("用户ID和设备ID不能为空");
        }
        if (booking.getStartTime() == null || booking.getEndTime() == null) {
            throw new IllegalArgumentException("预约时间不能为空");
        }
        if (booking.getStartTime().isAfter(booking.getEndTime())) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        // 1. 记录日志
        logger.info("创建预约 - 用户ID: {}, 设备ID: {}, 时间: {} - {}", 
                booking.getUserId(), booking.getEquipmentId(), 
                booking.getStartTime(), booking.getEndTime());
        
        // 2. 冲突校验
        Integer conflictCount = bookingMapper.countOverlappingBookings(
                booking.getEquipmentId(),
                booking.getStartTime(),
                booking.getEndTime(),
                null  // 新增预约时不需要排除
        );
        
        if (conflictCount != null && conflictCount > 0) {
            throw new BusinessException(400, "该时段已被预约，请选择其他时段");
        }
        
        // 3. 保存到数据库
        try {
            booking.setStatus(BookingStatus.PENDING.getCode());  // 设置为待审批状态
            booking.setCreateTime(LocalDateTime.now());
            booking.setUpdateTime(LocalDateTime.now());
            int result = bookingMapper.insert(booking);  // 调用 Mapper 插入数据
            boolean success = result > 0;
            
            if (success) {
                logger.info("创建预约成功 - 预约ID: {}", booking.getId());
            }
            return success;
            
        } catch (Exception e) {
            logger.error("创建预约失败", e);
            throw new BusinessException(500, "创建预约失败: " + e.getMessage());
        }
    }
    
    // ==================== 查询用户预约 ====================
    @Override
    public List<Booking> getBookingsByUserId(Long userId) {
        logger.info("查询用户预约 - 用户ID: {}", userId);
        try {
            return bookingMapper.selectByUserId(userId);
        } catch (Exception e) {
            logger.error("查询用户预约失败", e);
            throw new BusinessException(500, "查询预约失败: " + e.getMessage());
        }
    }
    
    // ==================== 查询用户预约（含设备名称） ====================
    @Override
    public List<BookingDTO> getBookingsByUserIdWithEquipment(Long userId) {
        logger.info("查询用户预约（含设备名称） - 用户ID: {}", userId);
        try {
            return bookingMapper.selectByUserIdWithEquipment(userId);
        } catch (Exception e) {
            logger.error("查询用户预约失败", e);
            throw new BusinessException(500, "查询预约失败: " + e.getMessage());
        }
    }
    
    // ==================== 查询待审批列表 ====================
    @Override
    public List<Booking> getPendingApprovals() {
        // 校验管理员权限
        checkAdminPermission();
        
        logger.info("查询待审批列表");
        try {
            return bookingMapper.selectPendingApprovals();
        } catch (Exception e) {
            logger.error("查询待审批列表失败", e);
            throw new BusinessException(500, "查询待审批列表失败: " + e.getMessage());
        }
    }
    
    // ==================== 查询待审批列表（含名称） ====================
    @Override
    public List<BookingDTO> getPendingApprovalsWithNames() {
        // 校验管理员权限
        checkAdminPermission();
        
        logger.info("查询待审批列表（含名称）");
        try {
            return bookingMapper.selectPendingApprovalsWithNames();
        } catch (Exception e) {
            logger.error("查询待审批列表失败", e);
            throw new BusinessException(500, "查询待审批列表失败: " + e.getMessage());
        }
    }
    
    // ==================== 审批预约 ====================
    @Override
    public boolean approveBooking(Long bookingId, boolean approve, String rejectReason) {
        // 参数校验
        if (bookingId == null) {
            throw new IllegalArgumentException("预约ID不能为空");
        }
        
        // 校验管理员权限
        checkAdminPermission();
        Long currentUserId = getCurrentUserId();
        
        logger.info("审批预约 - 预约ID: {}, 审批结果: {}, 审批人ID: {}", bookingId, approve ? "通过" : "拒绝", currentUserId);
        
        // 1. 查询预约信息
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预约不存在");
        }
        
        // 2. 状态校验（只能审批待审批的预约）
        if (booking.getStatusEnum() != BookingStatus.PENDING) {
            throw new BusinessException(400, "该预约已处理过");
        }
        
        // 3. 更新状态
        try {
            booking.setStatus(approve ? BookingStatus.APPROVED.getCode() : BookingStatus.REJECTED.getCode());
            booking.setApproverId(currentUserId);
            booking.setApproveTime(LocalDateTime.now());
            
            // 拒绝时必须填写原因
            if (!approve) {
                if (rejectReason == null || rejectReason.isEmpty()) {
                    throw new BusinessException(400, "拒绝预约需要填写原因");
                }
                booking.setRejectReason(rejectReason);
            }
            
            booking.setUpdateTime(LocalDateTime.now());
            int result = bookingMapper.updateById(booking);
            return result > 0;
            
        } catch (BusinessException e) {
            throw e;  // 业务异常直接抛出
        } catch (Exception e) {
            logger.error("审批失败", e);
            throw new BusinessException(500, "审批失败: " + e.getMessage());
        }
    }
    
    // ==================== 取消预约 ====================
    @Override
    public boolean cancelBooking(Long bookingId, Long userId) {
        // 参数校验
        if (bookingId == null || userId == null) {
            throw new IllegalArgumentException("预约ID和用户ID不能为空");
        }
        
        logger.info("取消预约 - 预约ID: {}, 用户ID: {}", bookingId, userId);
        
        // 1. 查询预约
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException(404, "预约不存在");
        }
        
        // 2. 权限校验（只能取消自己的预约）
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权取消他人预约");
        }
        
        // 3. 状态校验（只能取消待审批的预约）
        if (booking.getStatusEnum() != BookingStatus.PENDING) {
            throw new BusinessException(400, "只能取消待审批的预约");
        }
        
        // 4. 更新状态
        try {
            booking.setStatus(BookingStatus.CANCELLED.getCode());  // 已取消
            booking.setUpdateTime(LocalDateTime.now());
            return bookingMapper.updateById(booking) > 0;
            
        } catch (Exception e) {
            logger.error("取消预约失败", e);
            throw new BusinessException(500, "取消预约失败: " + e.getMessage());
        }
    }
    
    // ==================== 获取可用时段 ====================
    @Override
    public List<String> getAvailableSlots(Long equipmentId, String dateStr) {
        logger.info("获取可用时段 - 设备ID: {}, 日期: {}", equipmentId, dateStr);
        
        LocalDate date = LocalDate.parse(dateStr);
        List<String> availableSlots = new ArrayList<>();
        
        try {
            // 遍历所有预设时段，检查是否有冲突
            for (String slot : TIME_SLOTS) {
                String[] times = slot.split("-");
                LocalDateTime startTime = LocalDateTime.of(date, LocalTime.parse(times[0]));
                LocalDateTime endTime = LocalDateTime.of(date, LocalTime.parse(times[1]));
                
                // 检查该时段是否已被预约
                Integer conflictCount = bookingMapper.countOverlappingBookings(
                        equipmentId, startTime, endTime, null);
                
                if (conflictCount == null || conflictCount == 0) {
                    availableSlots.add(slot);
                }
            }
            return availableSlots;
            
        } catch (Exception e) {
            logger.error("获取可用时段失败", e);
            throw new BusinessException(500, "获取可用时段失败: " + e.getMessage());
        }
    }
    
    // ==================== 查询已通过预约（供使用记录模块调用）====================
    @Override
    public List<Booking> getApprovedBookings(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("查询已通过预约 - {} 至 {}", startTime, endTime);
        try {
            return bookingMapper.selectApprovedBookings(startTime, endTime);
        } catch (Exception e) {
            logger.error("查询已通过预约失败", e);
            throw new BusinessException(500, "查询预约失败: " + e.getMessage());
        }
    }
}