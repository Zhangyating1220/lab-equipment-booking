package com.lab.equipment_booking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment_booking.common.Result;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@CrossOrigin
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping("/list")
    public Result<Page<Equipment>> list(@Valid EquipmentQueryDTO queryDTO) {
        return Result.success(equipmentService.pageQuery(queryDTO));
    }

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody Equipment equipment) {
        boolean success = equipmentService.addEquipment(equipment);
        return success ? Result.success("添加成功", null) : Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody Equipment equipment) {
        boolean success = equipmentService.updateEquipment(equipment);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = equipmentService.deleteEquipment(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        // 返回设备类别列表
        List<String> categories = Arrays.asList("显微镜", "离心机", "PCR仪", "电子天平", "恒温培养箱");
        return Result.success(categories);
    }
}