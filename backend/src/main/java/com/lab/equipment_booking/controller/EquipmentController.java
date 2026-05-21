package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.common.Result;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin
public class EquipmentController {
    
    @Autowired
    private EquipmentService equipmentService;
    
    @GetMapping("/list")
    public Result<?> list(EquipmentQueryDTO dto) {
        return Result.success(equipmentService.pageQuery(dto));
    }
    
    @PostMapping("/add")
    public Result<?> add(@RequestBody Equipment equipment) {
        boolean success = equipmentService.addEquipment(equipment);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }
    
    @PutMapping("/update")
    public Result<?> update(@RequestBody Equipment equipment) {
        boolean success = equipmentService.updateEquipment(equipment);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
    
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = equipmentService.deleteEquipment(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}