package com.lab.equipment_booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment_booking.dto.BookingDTO;
import com.lab.equipment_booking.entity.Booking;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingMapper extends BaseMapper<Booking> {

    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE equipment_id = #{equipmentId} " +
            "AND status != 3 " +
            "AND start_time < #{newEndTime} " +
            "AND end_time > #{newStartTime} " +
            "AND (#{excludeBookingId} IS NULL OR id != #{excludeBookingId})")
    Integer countOverlappingBookings(
            @Param("equipmentId") Long equipmentId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime,
            @Param("excludeBookingId") Long excludeBookingId);

    @Select("SELECT * FROM booking WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Booking> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT b.* FROM booking b WHERE b.status = 0 ORDER BY b.create_time ASC")
    List<Booking> selectPendingApprovals();

    /**
     * 查询待审批预约（关联用户和设备表）
     */
    @Select("SELECT b.*, u.name as userName, e.name as equipmentName " +
            "FROM booking b " +
            "LEFT JOIN user u ON b.user_id = u.id " +
            "LEFT JOIN equipment e ON b.equipment_id = e.id " +
            "WHERE b.status = 0 ORDER BY b.create_time ASC")
    List<BookingDTO> selectPendingApprovalsWithNames();

    /**
     * 查询用户预约（关联设备表）
     */
    @Select("SELECT b.*, e.name as equipmentName " +
            "FROM booking b " +
            "LEFT JOIN equipment e ON b.equipment_id = e.id " +
            "WHERE b.user_id = #{userId} ORDER BY b.create_time DESC")
    List<BookingDTO> selectByUserIdWithEquipment(@Param("userId") Long userId);

    @Select("SELECT * FROM booking WHERE status = 1 AND start_time >= #{startTime} AND start_time < #{endTime}")
    List<Booking> selectApprovedBookings(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}