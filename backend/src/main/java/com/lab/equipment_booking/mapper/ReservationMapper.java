// 修改 ReservationMapper.java
package com.lab.equipment_booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment_booking.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    @Select("SELECT COUNT(*) FROM reservation WHERE equipment_id = #{equipmentId} " +
            "AND status = 1 AND start_time < #{endTime} AND end_time > #{startTime}")
    int countConflictingReservations(@Param("equipmentId") Long equipmentId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM reservation WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Reservation> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM reservation WHERE status = 0 ORDER BY create_time ASC")
    List<Reservation> findPendingApproval();

    /**
     * 自定义插入方法，确保所有字段都被插入
     */
    @Insert("INSERT INTO reservation (user_id, equipment_id, start_time, end_time, purpose, status, reject_reason, create_time, update_time) " +
            "VALUES (#{userId}, #{equipmentId}, #{startTime}, #{endTime}, #{purpose}, #{status}, #{rejectReason}, #{createTime}, #{updateTime})")
    int insertReservation(@Param("userId") Long userId,
                          @Param("equipmentId") Long equipmentId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("purpose") String purpose,
                          @Param("status") Integer status,
                          @Param("rejectReason") String rejectReason,
                          @Param("createTime") LocalDateTime createTime,
                          @Param("updateTime") LocalDateTime updateTime);
}