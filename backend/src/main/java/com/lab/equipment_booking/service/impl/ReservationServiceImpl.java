package com.lab.equipment_booking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.entity.Reservation;
import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.entity.UsageRecord;
import com.lab.equipment_booking.mapper.EquipmentMapper;
import com.lab.equipment_booking.mapper.ReservationMapper;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.ReservationService;
import com.lab.equipment_booking.service.UsageRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UsageRecordService usageRecordService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 定义一天的可选时段（每个时段1小时）
    private static final List<String> ALL_SLOTS = Arrays.asList(
        "08:00-09:00", "09:00-10:00", "10:00-11:00", "11:00-12:00",
        "14:00-15:00", "15:00-16:00", "16:00-17:00", "17:00-18:00"
    );

    @Override
    @Transactional
    public int createReservation(Reservation reservation) {
        try {
            logger.info("开始创建预约: userId={}, equipmentId={}, startTime={}, endTime={}",
                    reservation.getUserId(),
                    reservation.getEquipmentId(),
                    reservation.getStartTime(),
                    reservation.getEndTime());

            // 检查设备是否存在且可用
            Equipment equipment = equipmentMapper.selectById(reservation.getEquipmentId());
            if (equipment == null) {
                logger.error("设备不存在: equipmentId={}", reservation.getEquipmentId());
                return 2; // 设备不可用
            }
            if (equipment.getStatus() != 0) {
                logger.error("设备不可用: equipmentId={}, status={}", reservation.getEquipmentId(), equipment.getStatus());
                return 2; // 设备不可用
            }

            // 检查时段冲突（使用重叠时段判断算法：start_time < new_end AND end_time > new_start）
            if (reservation.getStartTime() == null || reservation.getEndTime() == null) {
                logger.error("预约时间为空");
                throw new RuntimeException("预约时间不能为空");
            }

            if (hasConflict(reservation.getEquipmentId(), 
                            reservation.getStartTime().format(FORMATTER), 
                            reservation.getEndTime().format(FORMATTER))) {
                logger.info("时段冲突: equipmentId={}, startTime={}, endTime={}",
                        reservation.getEquipmentId(),
                        reservation.getStartTime(),
                        reservation.getEndTime());
                return 1; // 时段冲突
            }

            // 设置状态为待审批
            reservation.setStatus(0);
            reservation.setCreateTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());
            
            // 使用自定义插入方法，确保所有字段都被正确插入
            reservationMapper.insertReservation(
                reservation.getUserId(),
                reservation.getEquipmentId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPurpose(),
                reservation.getStatus(),
                reservation.getRejectReason(),
                reservation.getCreateTime(),
                reservation.getUpdateTime()
            );
            
            logger.info("预约创建成功: userId={}, equipmentId={}", 
                    reservation.getUserId(), reservation.getEquipmentId());
            return 0; // 成功
            
        } catch (Exception e) {
            logger.error("创建预约失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建预约失败: " + e.getMessage());
        }
    }

    @Override
    public boolean hasConflict(Long equipmentId, String startTimeStr, String endTimeStr) {
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr, FORMATTER);
        int count = reservationMapper.countConflictingReservations(equipmentId, startTime, endTime);
        return count > 0;
    }

    @Override
    public List<Map<String, Object>> getUserReservations(Long userId) {
        List<Reservation> reservations = reservationMapper.findByUserId(userId);
        return reservations.stream().map(this::convertToMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPendingReservations() {
        List<Reservation> reservations = reservationMapper.findPendingApproval();
        return reservations.stream().map(this::convertToMap).collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableSlots(Long equipmentId, String date) {
        // 查询当天已被预约的时段
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        QueryWrapper<Reservation> wrapper = new QueryWrapper<>();
        wrapper.eq("equipment_id", equipmentId)
               .eq("status", 1)
               .ge("start_time", startOfDay)
               .lt("end_time", endOfDay);

        List<Reservation> reservations = reservationMapper.selectList(wrapper);

        // 找出已被占用的时段
        Set<String> occupiedSlots = new HashSet<>();
        for (Reservation res : reservations) {
            String start = res.getStartTime().format(TIME_FORMATTER);
            String end = res.getEndTime().format(TIME_FORMATTER);
            occupiedSlots.add(start + "-" + end);
        }

        // 返回可用时段（纯字符串列表，格式如 "09:00-10:00"）
        List<String> availableSlots = new ArrayList<>();
        for (String slot : ALL_SLOTS) {
            if (!occupiedSlots.contains(slot)) {
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    @Override
    @Transactional
    public boolean approveReservation(Long id) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null || reservation.getStatus() != 0) {
            return false;
        }
        reservation.setStatus(1);
        reservation.setUpdateTime(LocalDateTime.now());
        
        boolean success = reservationMapper.updateById(reservation) > 0;
        
        if (success) {
            Equipment equipment = equipmentMapper.selectById(reservation.getEquipmentId());
            User user = userMapper.selectById(reservation.getUserId());
            
            UsageRecord record = new UsageRecord();
            record.setReservationId(reservation.getId());
            record.setEquipmentId(reservation.getEquipmentId());
            record.setEquipmentName(equipment != null ? equipment.getName() : "");
            record.setUserId(reservation.getUserId());
            record.setUserName(user != null ? user.getName() : "");
            record.setStartTime(reservation.getStartTime());
            record.setEndTime(reservation.getEndTime());
            record.setStatus(0);
            
            usageRecordService.createRecord(record);
            logger.info("审批通过，已创建使用记录: reservationId={}", id);
        }
        
        return success;
    }

    @Override
    @Transactional
    public boolean rejectReservation(Long id, String reason) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null || reservation.getStatus() != 0) {
            return false;
        }
        reservation.setStatus(2);
        reservation.setRejectReason(reason);
        reservation.setUpdateTime(LocalDateTime.now());
        return reservationMapper.updateById(reservation) > 0;
    }

    @Override
    @Transactional
    public boolean cancelReservation(Long id) {
        Reservation reservation = reservationMapper.selectById(id);
        if (reservation == null) {
            return false;
        }
        if (reservation.getStatus() != 0 && reservation.getStatus() != 1) {
            return false;
        }
        reservation.setStatus(3);
        reservation.setUpdateTime(LocalDateTime.now());
        return reservationMapper.updateById(reservation) > 0;
    }

    private Map<String, Object> convertToMap(Reservation reservation) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", reservation.getId());
        map.put("userId", reservation.getUserId());
        map.put("equipmentId", reservation.getEquipmentId());

        Equipment equipment = equipmentMapper.selectById(reservation.getEquipmentId());
        if (equipment != null) {
            map.put("equipmentName", equipment.getName());
            map.put("equipmentModel", equipment.getModel());
            map.put("equipmentLocation", equipment.getLocation());
        }

        // 加载用户信息
        User user = userMapper.selectById(reservation.getUserId());
        if (user != null) {
            map.put("userName", user.getName() != null ? user.getName() : user.getUsername());
            logger.info("查询到用户: userId={}, name={}, username={}", reservation.getUserId(), user.getName(), user.getUsername());
        } else {
            map.put("userName", "未知用户");
            logger.warn("未找到用户: userId={}", reservation.getUserId());
        }

        map.put("startTime", reservation.getStartTime());
        map.put("endTime", reservation.getEndTime());
        map.put("purpose", reservation.getPurpose());
        map.put("status", reservation.getStatus());
        map.put("statusText", getStatusText(reservation.getStatus()));
        map.put("rejectReason", reservation.getRejectReason());
        map.put("createTime", reservation.getCreateTime());
        return map;
    }

    private String getStatusText(Integer status) {
        switch (status) {
            case 0: return "待审批";
            case 1: return "已通过";
            case 2: return "已拒绝";
            case 3: return "已取消";
            default: return "未知";
        }
    }
}