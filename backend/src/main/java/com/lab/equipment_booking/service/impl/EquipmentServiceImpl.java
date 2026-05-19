package com.lab.equipment_booking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.exception.BusinessException;
import com.lab.equipment_booking.mapper.EquipmentMapper;
import com.lab.equipment_booking.service.EquipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentServiceImpl.class);

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Override
    public Page<Equipment> pageQuery(EquipmentQueryDTO dto) {
        logger.info("查询设备列表 - 页码: {}, 每页数量: {}, 类别: {}, 状态: {}, 名称: {}",
                dto.getPageNum(), dto.getPageSize(), dto.getCategory(), dto.getStatus(), dto.getName());
        try {
            Page<Equipment> page = new Page<>(dto.getPageNum(), dto.getPageSize());
            LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(dto.getCategory())) {
                wrapper.eq(Equipment::getCategory, dto.getCategory());
            }
            if (dto.getStatus() != null) {
                wrapper.eq(Equipment::getStatus, dto.getStatus());
            }
            if (StringUtils.hasText(dto.getName())) {
                wrapper.like(Equipment::getName, dto.getName());
            }
            wrapper.orderByDesc(Equipment::getCreateTime);
            Page<Equipment> result = equipmentMapper.selectPage(page, wrapper);
            logger.info("查询设备列表成功 - 总记录数: {}, 当前页数量: {}", result.getTotal(), result.getSize());
            return result;
        } catch (Exception e) {
            logger.error("查询设备列表失败", e);
            throw new BusinessException(500, "查询设备列表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean addEquipment(Equipment equipment) {
        logger.info("添加设备 - 名称: {}, 型号: {}, 位置: {}", 
                equipment.getName(), equipment.getModel(), equipment.getLocation());
        try {
            equipment.setCreateTime(LocalDateTime.now());
            int result = equipmentMapper.insert(equipment);
            boolean success = result > 0;
            if (success) {
                logger.info("添加设备成功 - 设备ID: {}", equipment.getId());
            } else {
                logger.warn("添加设备失败 - 名称: {}", equipment.getName());
            }
            return success;
        } catch (Exception e) {
            logger.error("添加设备失败 - 名称: {}", equipment.getName(), e);
            throw new BusinessException(500, "添加设备失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateEquipment(Equipment equipment) {
        logger.info("更新设备 - ID: {}, 名称: {}", equipment.getId(), equipment.getName());
        try {
            int result = equipmentMapper.updateById(equipment);
            boolean success = result > 0;
            if (success) {
                logger.info("更新设备成功 - ID: {}", equipment.getId());
            } else {
                logger.warn("更新设备失败 - ID: {}", equipment.getId());
            }
            return success;
        } catch (Exception e) {
            logger.error("更新设备失败 - ID: {}", equipment.getId(), e);
            throw new BusinessException(500, "更新设备失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteEquipment(Long id) {
        logger.info("删除设备 - ID: {}", id);
        try {
            int result = equipmentMapper.deleteById(id);
            boolean success = result > 0;
            if (success) {
                logger.info("删除设备成功 - ID: {}", id);
            } else {
                logger.warn("删除设备失败 - ID: {}", id);
            }
            return success;
        } catch (Exception e) {
            logger.error("删除设备失败 - ID: {}", id, e);
            throw new BusinessException(500, "删除设备失败: " + e.getMessage());
        }
    }
}