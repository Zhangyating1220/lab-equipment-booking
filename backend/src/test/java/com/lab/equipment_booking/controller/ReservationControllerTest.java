package com.lab.equipment_booking.controller;

import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.entity.Reservation;
import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.mapper.EquipmentMapper;
import com.lab.equipment_booking.mapper.ReservationMapper;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.EquipmentService;
import com.lab.equipment_booking.service.ReservationService;
import com.lab.equipment_booking.service.UserService;
import com.lab.equipment_booking.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String generateToken(Long userId, String username, Integer role) {
        return jwtUtil.generateToken(userId, username, role);
    }

    private User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setName("测试用户");
        user.setRole(0);
        userService.register(user);
        return userMapper.findByUsername(username);
    }

    private Equipment createTestEquipment(String name, int status) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setCategory("测试类别");
        equipment.setModel("TEST-001");
        equipment.setLocation("测试位置");
        equipment.setStatus(status);
        equipmentService.addEquipment(equipment);
        return equipmentMapper.selectById(equipment.getId());
    }

    @Test
    void testApproveReservation_Success() throws Exception {
        User user = createTestUser("approve_api_user_001");
        Equipment equipment = createTestEquipment("审批API设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(3).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

        User admin = new User();
        admin.setUsername("admin_approve_001");
        admin.setPassword("admin123");
        admin.setName("审批管理员");
        admin.setRole(1);
        userService.register(admin);

        String token = generateToken(admin.getId(), admin.getUsername(), admin.getRole());

        mockMvc.perform(put("/api/reservation/approve/" + reservation.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testRejectReservation_Success() throws Exception {
        User user = createTestUser("reject_api_user_001");
        Equipment equipment = createTestEquipment("拒绝API设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(4).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(4).withHour(10).withMinute(0));
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

        User admin = new User();
        admin.setUsername("admin_reject_001");
        admin.setPassword("admin123");
        admin.setName("拒绝管理员");
        admin.setRole(1);
        userService.register(admin);

        String token = generateToken(admin.getId(), admin.getUsername(), admin.getRole());

        String rejectJson = "{\"reason\":\"设备需要维修\"}";

        mockMvc.perform(put("/api/reservation/reject/" + reservation.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rejectJson))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelReservation_Success() throws Exception {
        User user = createTestUser("cancel_api_user_001");
        Equipment equipment = createTestEquipment("取消API设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(5).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0));
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

        mockMvc.perform(put("/api/reservation/cancel/" + reservation.getId()))
                .andExpect(status().isOk());
    }
}
