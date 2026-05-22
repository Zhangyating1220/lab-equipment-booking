package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.common.Result;
import com.lab.equipment_booking.entity.UsageRecord;
import com.lab.equipment_booking.service.UsageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usage")
@CrossOrigin
public class UsageRecordController {
    
    @Autowired
    private UsageRecordService usageRecordService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        return Result.success(usageRecordService.findAllWithDetail());
    }

    @GetMapping("/user/{userId}")
    public Result<List<UsageRecord>> getUserRecords(@PathVariable Long userId) {
        return Result.success(usageRecordService.findByUserId(userId));
    }

    @GetMapping("/equipment/{equipmentId}")
    public Result<List<UsageRecord>> getEquipmentRecords(@PathVariable Long equipmentId) {
        return Result.success(usageRecordService.findByEquipmentId(equipmentId));
    }

    @PostMapping("/create")
    public Result<Void> create(@RequestBody UsageRecord record) {
        boolean success = usageRecordService.createRecord(record);
        return success ? Result.success("创建成功", null) : Result.error("创建失败");
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody UsageRecord record) {
        boolean success = usageRecordService.updateRecord(record);
        return success ? Result.success("更新成功", null) : Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = usageRecordService.deleteRecord(id);
        return success ? Result.success("删除成功", null) : Result.error("删除失败");
    }

    @PostMapping("/start/{id}")
    @GetMapping("/start/{id}")
    public Result<Void> startUsage(@PathVariable Long id) {
        boolean success = usageRecordService.startUsage(id);
        return success ? Result.success("开始使用", null) : Result.error("操作失败");
    }

    @PostMapping("/end/{id}")
    @GetMapping("/end/{id}")
    public Result<Void> endUsage(@PathVariable Long id) {
        boolean success = usageRecordService.endUsage(id);
        return success ? Result.success("使用结束", null) : Result.error("操作失败");
    }

    @GetMapping("/stats/equipment")
    public Result<List<Map<String, Object>>> getEquipmentStats() {
        return Result.success(usageRecordService.getEquipmentStats());
    }

    @GetMapping("/stats/user")
    public Result<List<Map<String, Object>>> getUserStats() {
        return Result.success(usageRecordService.getUserStats());
    }

    @GetMapping("/stats/summary")
    public Result<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("equipmentStats", usageRecordService.getEquipmentStats());
        summary.put("userStats", usageRecordService.getUserStats());
        return Result.success(summary);
    }
}