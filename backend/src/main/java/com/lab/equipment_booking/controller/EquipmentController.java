package com.lab.equipment_booking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping("/list")
    public Page<Equipment> list(EquipmentQueryDTO queryDTO) {
        return equipmentService.pageQuery(queryDTO);
    }

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Equipment equipment) {
        boolean success = equipmentService.addEquipment(equipment);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "添加成功" : "添加失败");
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody Equipment equipment) {
        boolean success = equipmentService.updateEquipment(equipment);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败");
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        boolean success = equipmentService.deleteEquipment(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }
}