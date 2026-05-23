package com.lab.equipment_booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment_booking.entity.UsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UsageRecordMapper extends BaseMapper<UsageRecord> {
    
    @Select("SELECT * FROM usage_record WHERE user_id = #{userId} ORDER BY start_time DESC")
    List<UsageRecord> findByUserId(Long userId);
    
    @Select("SELECT * FROM usage_record WHERE equipment_id = #{equipmentId} ORDER BY start_time DESC")
    List<UsageRecord> findByEquipmentId(Long equipmentId);
    
    @Select("SELECT * FROM usage_record WHERE status = #{status} ORDER BY start_time DESC")
    List<UsageRecord> findByStatus(Integer status);
    
    @Select("SELECT ur.id, ur.user_id, ur.equipment_id, ur.start_time, ur.end_time, " +
            "ur.actual_start_time, ur.actual_end_time, ur.status, ur.create_time, " +
            "e.name as equipment_name, u.name as user_name " +
            "FROM usage_record ur " +
            "LEFT JOIN equipment e ON ur.equipment_id = e.id " +
            "LEFT JOIN user u ON ur.user_id = u.id " +
            "ORDER BY ur.start_time DESC")
    List<Map<String, Object>> findAllWithDetail();
    
    @Select("SELECT COUNT(*) as total, SUM(TIMESTAMPDIFF(MINUTE, actual_start_time, actual_end_time)) as total_minutes " +
            "FROM usage_record WHERE status = 2 AND DATE(create_time) = #{date}")
    Map<String, Object> getDailyStats(LocalDateTime date);
    
    @Select("SELECT e.name as equipment_name, COUNT(*) as usage_count, " +
            "COALESCE(SUM(TIMESTAMPDIFF(MINUTE, actual_start_time, actual_end_time)), 0) as total_minutes " +
            "FROM usage_record ur " +
            "LEFT JOIN equipment e ON ur.equipment_id = e.id " +
            "GROUP BY ur.equipment_id, e.name " +
            "ORDER BY usage_count DESC")
    List<Map<String, Object>> getEquipmentStats();
    
    @Select("SELECT u.name as user_name, COUNT(*) as usage_count, " +
            "COALESCE(SUM(TIMESTAMPDIFF(MINUTE, actual_start_time, actual_end_time)), 0) as total_minutes " +
            "FROM usage_record ur " +
            "LEFT JOIN user u ON ur.user_id = u.id " +
            "GROUP BY ur.user_id, u.name " +
            "ORDER BY usage_count DESC")
    List<Map<String, Object>> getUserStats();
}