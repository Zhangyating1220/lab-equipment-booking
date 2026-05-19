package com.lab.equipment_booking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.mapper.EquipmentMapper;
import com.lab.equipment_booking.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Override
    public Page<Equipment> pageQuery(EquipmentQueryDTO dto) {
        Page<Equipment> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Equipment> wrapper = new LambdaQueryWrapper<>();
        // 按类别筛选
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.eq(Equipment::getCategory, dto.getCategory());
        }
        // 按状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(Equipment::getStatus, dto.getStatus());
        }
        // 按名称模糊搜索
        if (StringUtils.hasText(dto.getName())) {
            wrapper.like(Equipment::getName, dto.getName());
        }
        wrapper.orderByDesc(Equipment::getCreateTime);
        return equipmentMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean addEquipment(Equipment equipment) {
        equipment.setCreateTime(java.time.LocalDateTime.now());
        return equipmentMapper.insert(equipment) > 0;
    }

    @Override
    public boolean updateEquipment(Equipment equipment) {
        return equipmentMapper.updateById(equipment) > 0;
    }

    @Override
    public boolean deleteEquipment(Long id) {
        return equipmentMapper.deleteById(id) > 0;
    }
}