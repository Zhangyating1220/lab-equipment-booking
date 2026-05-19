package com.lab.equipment_booking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;

public interface EquipmentService {
    Page<Equipment> pageQuery(EquipmentQueryDTO queryDTO);
    boolean addEquipment(Equipment equipment);
    boolean updateEquipment(Equipment equipment);
    boolean deleteEquipment(Long id);
}