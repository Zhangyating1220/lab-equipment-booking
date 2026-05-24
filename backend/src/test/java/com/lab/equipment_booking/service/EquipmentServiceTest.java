package com.lab.equipment_booking.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.mapper.EquipmentMapper;
import com.lab.equipment_booking.service.impl.EquipmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EquipmentServiceTest {

    @Autowired
    private EquipmentServiceImpl equipmentService;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Test
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
    void testDeleteEquipment_NotFound() {
        boolean result = equipmentService.deleteEquipment(99999L);
        assertFalse(result);
    }

    @Test
    void testPageQuery_AllEquipments() {
        for (int i = 0; i < 5; i++) {
            Equipment equipment = new Equipment();
            equipment.setName("设备" + i);
            equipment.setCategory("类别A");
            equipment.setStatus(0);
            equipmentMapper.insert(equipment);
        }

        EquipmentQueryDTO dto = new EquipmentQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        Page<Equipment> page = equipmentService.pageQuery(dto);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 5);
    }

    @Test
    void testPageQuery_FilterByCategory() {
        Equipment equipment1 = new Equipment();
        equipment1.setName("显微镜A");
        equipment1.setCategory("显微镜");
        equipment1.setStatus(0);
        equipmentMapper.insert(equipment1);

        Equipment equipment2 = new Equipment();
        equipment2.setName("离心机A");
        equipment2.setCategory("离心机");
        equipment2.setStatus(0);
        equipmentMapper.insert(equipment2);

        EquipmentQueryDTO dto = new EquipmentQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setCategory("显微镜");

        Page<Equipment> page = equipmentService.pageQuery(dto);
        assertNotNull(page);
        assertTrue(page.getRecords().stream()
                .allMatch(e -> "显微镜".equals(e.getCategory())));
    }

    @Test
    void testPageQuery_FilterByStatus() {
        Equipment equipment1 = new Equipment();
        equipment1.setName("可用设备");
        equipment1.setCategory("其它");
        equipment1.setStatus(0);
        equipmentMapper.insert(equipment1);

        Equipment equipment2 = new Equipment();
        equipment2.setName("维修中设备");
        equipment2.setCategory("其它");
        equipment2.setStatus(1);
        equipmentMapper.insert(equipment2);

        EquipmentQueryDTO dto = new EquipmentQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setStatus(0);

        Page<Equipment> page = equipmentService.pageQuery(dto);
        assertNotNull(page);
        assertTrue(page.getRecords().stream()
                .allMatch(e -> e.getStatus() == 0));
    }

    @Test
    void testPageQuery_Pagination() {
        for (int i = 0; i < 15; i++) {
            Equipment equipment = new Equipment();
            equipment.setName("分页测试设备" + i);
            equipment.setCategory("分页测试");
            equipment.setStatus(0);
            equipmentMapper.insert(equipment);
        }

        EquipmentQueryDTO dto1 = new EquipmentQueryDTO();
        dto1.setPageNum(1);
        dto1.setPageSize(10);

        Page<Equipment> page1 = equipmentService.pageQuery(dto1);
        assertEquals(10, page1.getRecords().size());

        EquipmentQueryDTO dto2 = new EquipmentQueryDTO();
        dto2.setPageNum(2);
        dto2.setPageSize(10);

        Page<Equipment> page2 = equipmentService.pageQuery(dto2);
        assertTrue(page2.getRecords().size() >= 5);
    }
}
