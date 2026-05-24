package com.lab.equipment_booking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.mapper.EquipmentMapper;
import com.lab.equipment_booking.service.impl.EquipmentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EquipmentServiceTest {

    @Autowired
    private EquipmentServiceImpl equipmentService;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Test
    @DisplayName("添加设备-成功")
    void testAddEquipment_Success() {
        Equipment equipment = new Equipment();
        equipment.setName("测试显微镜");
        equipment.setCategory("显微镜");
        equipment.setModel("CX-2000");
        equipment.setLocation("实验楼A-101");
        equipment.setStatus(0);
        equipment.setDescription("高精度显微镜");

        boolean result = equipmentService.addEquipment(equipment);
        assertTrue(result);
        assertNotNull(equipment.getId());
    }

    @Test
    @DisplayName("更新设备-成功")
    void testUpdateEquipment_Success() {
        Equipment equipment = new Equipment();
        equipment.setName("原设备名");
        equipment.setCategory("离心机");
        equipment.setModel("L-500");
        equipment.setLocation("实验楼B-201");
        equipment.setStatus(0);
        equipmentMapper.insert(equipment);

        equipment.setName("修改后的设备名");
        equipment.setLocation("实验楼C-301");

        boolean result = equipmentService.updateEquipment(equipment);
        assertTrue(result);

        Equipment updated = equipmentMapper.selectById(equipment.getId());
        assertEquals("修改后的设备名", updated.getName());
        assertEquals("实验楼C-301", updated.getLocation());
    }

    @Test
    @DisplayName("更新设备-状态变更")
    void testUpdateEquipment_StatusChange() {
        Equipment equipment = new Equipment();
        equipment.setName("状态变更测试设备");
        equipment.setCategory("其它");
        equipment.setStatus(0);
        equipmentMapper.insert(equipment);

        equipment.setStatus(1);
        boolean result = equipmentService.updateEquipment(equipment);
        assertTrue(result);

        Equipment updated = equipmentMapper.selectById(equipment.getId());
        assertEquals(1, updated.getStatus());
    }

    @Test
    @DisplayName("删除设备-成功")
    void testDeleteEquipment_Success() {
        Equipment equipment = new Equipment();
        equipment.setName("待删除设备");
        equipment.setCategory("其它");
        equipment.setStatus(0);
        equipmentMapper.insert(equipment);

        Long id = equipment.getId();
        boolean result = equipmentService.deleteEquipment(id);
        assertTrue(result);

        Equipment deleted = equipmentMapper.selectById(id);
        assertNull(deleted);
    }

    @Test
    @DisplayName("删除设备-不存在")
    void testDeleteEquipment_NotFound() {
        boolean result = equipmentService.deleteEquipment(99999L);
        assertFalse(result);
    }

    @Test
    @DisplayName("分页查询-所有设备")
    void testPageQuery_AllEquipments() {
        String uniqueCategory = "测试类别_" + UUID.randomUUID().toString().substring(0, 8);
        
        for (int i = 0; i < 5; i++) {
            Equipment equipment = new Equipment();
            equipment.setName("设备" + i);
            equipment.setCategory(uniqueCategory);
            equipment.setStatus(0);
            equipmentMapper.insert(equipment);
        }

        EquipmentQueryDTO dto = new EquipmentQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setCategory(uniqueCategory);

        Page<Equipment> page = equipmentService.pageQuery(dto);
        assertNotNull(page);
        assertEquals(5, page.getTotal());
    }

    @Test
    @DisplayName("分页查询-按类别筛选")
    void testPageQuery_FilterByCategory() {
        String uniqueCategory = "显微镜_" + UUID.randomUUID().toString().substring(0, 8);
        
        Equipment equipment1 = new Equipment();
        equipment1.setName("显微镜A");
        equipment1.setCategory(uniqueCategory);
        equipment1.setStatus(0);
        equipmentMapper.insert(equipment1);

        EquipmentQueryDTO dto = new EquipmentQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setCategory(uniqueCategory);

        Page<Equipment> page = equipmentService.pageQuery(dto);
        assertNotNull(page);
        assertEquals(1, page.getTotal());
        assertEquals("显微镜A", page.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("分页查询-按状态筛选")
    void testPageQuery_FilterByStatus() {
        String uniqueCategory = "状态测试_" + UUID.randomUUID().toString().substring(0, 8);
        
        Equipment equipment1 = new Equipment();
        equipment1.setName("可用设备");
        equipment1.setCategory(uniqueCategory);
        equipment1.setStatus(0);
        equipmentMapper.insert(equipment1);

        Equipment equipment2 = new Equipment();
        equipment2.setName("维修中设备");
        equipment2.setCategory(uniqueCategory);
        equipment2.setStatus(1);
        equipmentMapper.insert(equipment2);

        EquipmentQueryDTO dto = new EquipmentQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setCategory(uniqueCategory);
        dto.setStatus(0);

        Page<Equipment> page = equipmentService.pageQuery(dto);
        assertNotNull(page);
        assertEquals(1, page.getTotal());
        assertEquals("可用设备", page.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("分页查询-分页功能")
    void testPageQuery_Pagination() {
        String uniqueCategory = "分页测试_" + UUID.randomUUID().toString().substring(0, 8);
        
        for (int i = 0; i < 15; i++) {
            Equipment equipment = new Equipment();
            equipment.setName("分页测试设备" + i);
            equipment.setCategory(uniqueCategory);
            equipment.setStatus(0);
            equipmentMapper.insert(equipment);
        }

        EquipmentQueryDTO dto1 = new EquipmentQueryDTO();
        dto1.setPageNum(1);
        dto1.setPageSize(10);
        dto1.setCategory(uniqueCategory);

        Page<Equipment> page1 = equipmentService.pageQuery(dto1);
        assertEquals(10, page1.getRecords().size());

        EquipmentQueryDTO dto2 = new EquipmentQueryDTO();
        dto2.setPageNum(2);
        dto2.setPageSize(10);
        dto2.setCategory(uniqueCategory);

        Page<Equipment> page2 = equipmentService.pageQuery(dto2);
        assertEquals(5, page2.getRecords().size());
    }
}