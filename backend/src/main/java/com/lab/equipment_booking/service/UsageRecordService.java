package com.lab.equipment_booking.service;

import com.lab.equipment_booking.entity.UsageRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UsageRecordService {
    
    boolean createRecord(UsageRecord record);
    
    boolean updateRecord(UsageRecord record);
    
    boolean deleteRecord(Long id);
    
    UsageRecord findById(Long id);
    
    List<UsageRecord> findByUserId(Long userId);
    
    List<UsageRecord> findByEquipmentId(Long equipmentId);
    
    List<Map<String, Object>> findAllWithDetail();
    
    Map<String, Object> getDailyStats(LocalDateTime date);
    
    List<Map<String, Object>> getEquipmentStats();
    
    List<Map<String, Object>> getUserStats();
    
    boolean startUsage(Long recordId);
    
    boolean endUsage(Long recordId);
    
    void releaseTimeoutRecords();
}