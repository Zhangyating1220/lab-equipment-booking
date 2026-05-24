package com.lab.equipment_booking.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.equipment_booking.dto.EquipmentQueryDTO;
import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.mapper.EquipmentMapper;
import com.lab.equipment_booking.service.EquipmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Test
    @DisplayName("设备列表查询 - 成功场景")
    void testListEquipments_Success() throws Exception {
        for (int i = 0; i < 3; i++) {
            Equipment equipment = new Equipment();
            equipment.setName("列表测试设备" + i);
            equipment.setCategory("测试类别");
            equipment.setModel("TEST-00" + i);
            equipment.setLocation("测试位置" + i);
            equipment.setStatus(0);
            equipmentService.addEquipment(equipment);
        }

        mockMvc.perform(get("/api/equipment/list")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("设备列表查询 - 按分类筛选")
    void testListEquipments_WithCategoryFilter() throws Exception {
        Equipment equipment1 = new Equipment();
        equipment1.setName("显微镜A");
        equipment1.setCategory("显微镜");
        equipment1.setStatus(0);
        equipmentService.addEquipment(equipment1);

        Equipment equipment2 = new Equipment();
        equipment2.setName("离心机A");
        equipment2.setCategory("离心机");
        equipment2.setStatus(0);
        equipmentService.addEquipment(equipment2);

        mockMvc.perform(get("/api/equipment/list")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("category", "显微镜"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("设备列表查询 - 按状态筛选")
    void testListEquipments_WithStatusFilter() throws Exception {
        Equipment equipment1 = new Equipment();
        equipment1.setName("可用设备");
        equipment1.setCategory("测试");
        equipment1.setStatus(0);
        equipmentService.addEquipment(equipment1);

        Equipment equipment2 = new Equipment();
        equipment2.setName("维修中设备");
        equipment2.setCategory("测试");
        equipment2.setStatus(1);
        equipmentService.addEquipment(equipment2);

        mockMvc.perform(get("/api/equipment/list")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("添加设备 - 成功场景")
    void testAddEquipment_Success() throws Exception {
        String equipmentJson = "{\"name\":\"新增测试设备\",\"category\":\"测试类别\",\"model\":\"NEW-001\",\"location\":\"新位置\",\"status\":0,\"description\":\"测试描述\"}";

        mockMvc.perform(post("/api/equipment/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(equipmentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("更新设备 - 成功场景")
    void testUpdateEquipment_Success() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setName("待更新设备");
        equipment.setCategory("测试");
        equipment.setStatus(0);
        equipmentService.addEquipment(equipment);

        String updateJson = "{\"id\":" + equipment.getId() + ",\"name\":\"已更新设备\",\"category\":\"新类别\"}";

        mockMvc.perform(put("/api/equipment/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
