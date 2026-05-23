package com.lab.equipment_booking.service.impl;

import com.lab.equipment_booking.entity.UsageRecord;
import com.lab.equipment_booking.exception.BusinessException;
import com.lab.equipment_booking.mapper.UsageRecordMapper;
import com.lab.equipment_booking.service.UsageRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UsageRecordServiceImpl implements UsageRecordService {
    private static final Logger logger = LoggerFactory.getLogger(UsageRecordServiceImpl.class);
    
    @Autowired
    private UsageRecordMapper usageRecordMapper;

    @Override
    public boolean createRecord(UsageRecord record) {
        try {
            record.setCreateTime(LocalDateTime.now());
            return usageRecordMapper.insert(record) > 0;
        } catch (Exception e) {
            logger.error("创建使用记录失败", e);
            throw new BusinessException(500, "创建使用记录失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateRecord(UsageRecord record) {
        try {
            return usageRecordMapper.updateById(record) > 0;
        } catch (Exception e) {
            logger.error("更新使用记录失败, id: {}", record.getId(), e);
            throw new BusinessException(500, "更新使用记录失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteRecord(Long id) {
        try {
            return usageRecordMapper.deleteById(id) > 0;
        } catch (Exception e) {
            logger.error("删除使用记录失败, id: {}", id, e);
            throw new BusinessException(500, "删除使用记录失败: " + e.getMessage());
        }
    }

    @Override
    public UsageRecord findById(Long id) {
        try {
            return usageRecordMapper.selectById(id);
        } catch (Exception e) {
            logger.error("查询使用记录失败, id: {}", id, e);
            throw new BusinessException(500, "查询使用记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<UsageRecord> findByUserId(Long userId) {
        try {
            return usageRecordMapper.findByUserId(userId);
        } catch (Exception e) {
            logger.error("查询用户使用记录失败, userId: {}", userId, e);
            throw new BusinessException(500, "查询用户使用记录失败: " + e.getMessage());
        }
    }

    @Override
    public List<UsageRecord> findByEquipmentId(Long equipmentId) {
        try {
            return usageRecordMapper.findByEquipmentId(equipmentId);
        } catch (Exception e) {
            logger.error("查询设备使用记录失败, equipmentId: {}", equipmentId, e);
            throw new BusinessException(500, "查询设备使用记录失败: " + e.getMessage());
        }
    }

    @Override
public List<Map<String, Object>> findAllWithDetail() {
    try {
        return usageRecordMapper.findAllWithDetail();
    } catch (Exception e) {
        logger.error("查询所有使用记录失败", e);
        throw new BusinessException(500, "查询使用记录失败: " + e.getMessage());
    }
}

    @Override
    public Map<String, Object> getDailyStats(LocalDateTime date) {
        try {
            return usageRecordMapper.getDailyStats(date);
        } catch (Exception e) {
            logger.error("获取日统计失败", e);
            throw new BusinessException(500, "获取统计数据失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getEquipmentStats() {
        try {
            return usageRecordMapper.getEquipmentStats();
        } catch (Exception e) {
            logger.error("获取设备统计失败", e);
            throw new BusinessException(500, "获取设备统计失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getUserStats() {
        try {
            return usageRecordMapper.getUserStats();
        } catch (Exception e) {
            logger.error("获取用户统计失败", e);
            throw new BusinessException(500, "获取用户统计失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean startUsage(Long recordId) {
        try {
            logger.info("开始使用, recordId: {}", recordId);
            UsageRecord record = usageRecordMapper.selectById(recordId);
            if (record == null) {
                logger.warn("使用记录不存在, recordId: {}", recordId);
                throw new BusinessException(400, "使用记录不存在");
            }
            logger.info("当前状态: {}, userId: {}, equipmentId: {}", record.getStatus(), record.getUserId(), record.getEquipmentId());
            if (record.getStatus() != 0) {
                logger.warn("当前状态不允许开始使用, recordId: {}, status: {}", recordId, record.getStatus());
                throw new BusinessException(400, "当前状态不允许开始使用");
            }
            // 检查预约是否已过期
            LocalDateTime now = LocalDateTime.now();
            if (record.getEndTime() != null && record.getEndTime().isBefore(now)) {
                logger.warn("预约已过期, recordId: {}, endTime: {}", recordId, record.getEndTime());
                throw new BusinessException(400, "预约已过期，无法开始使用");
            }
            record.setStatus(1);
            record.setActualStartTime(now);
            int affectedRows = usageRecordMapper.updateById(record);
            logger.info("更新结果: affectedRows={}", affectedRows);
            return affectedRows > 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("开始使用失败, recordId: {}", recordId, e);
            throw new BusinessException(500, "开始使用失败: " + e.getMessage());
        }
    }

    @Override
    public boolean endUsage(Long recordId) {
        try {
            UsageRecord record = usageRecordMapper.selectById(recordId);
            if (record == null) {
                throw new BusinessException(400, "使用记录不存在");
            }
            if (record.getStatus() != 1) {
                throw new BusinessException(400, "当前状态不允许结束使用");
            }
            record.setStatus(2);
            record.setActualEndTime(LocalDateTime.now());
            return usageRecordMapper.updateById(record) > 0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("结束使用失败, recordId: {}", recordId, e);
            throw new BusinessException(500, "结束使用失败: " + e.getMessage());
        }
    }

    @Override
    public void releaseTimeoutRecords() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<UsageRecord> records = usageRecordMapper.findByStatus(1);
            for (UsageRecord record : records) {
                if (record.getEndTime() != null && record.getEndTime().isBefore(now)) {
                    record.setStatus(3);
                    record.setActualEndTime(now);
                    usageRecordMapper.updateById(record);
                    logger.info("自动释放超时使用记录: {}", record.getId());
                }
            }
        } catch (Exception e) {
            logger.error("释放超时记录失败", e);
        }
    }
}