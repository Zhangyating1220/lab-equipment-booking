package com.lab.equipment_booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment_booking.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {
}