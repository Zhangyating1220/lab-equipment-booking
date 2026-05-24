package com.lab.equipment_booking.service;

import com.lab.equipment_booking.entity.Equipment;
import com.lab.equipment_booking.entity.Reservation;
import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.mapper.EquipmentMapper;
import com.lab.equipment_booking.mapper.ReservationMapper;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.impl.EquipmentServiceImpl;
import com.lab.equipment_booking.service.impl.ReservationServiceImpl;
import com.lab.equipment_booking.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationServiceImpl reservationService;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private EquipmentServiceImpl equipmentService;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
    @DisplayName("创建预约 - 成功场景")
    void testCreateReservation_Success() {
        User user = createTestUser("res_user_001");
        Equipment equipment = createTestEquipment("预约测试设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        reservation.setPurpose("实验需要");
        reservation.setStatus(0);

        int result = reservationService.createReservation(reservation);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("创建预约 - 设备不可用")
    void testCreateReservation_EquipmentNotAvailable() {
        User user = createTestUser("res_user_002");
        Equipment equipment = createTestEquipment("不可用设备", 1);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        reservation.setStatus(0);

        int result = reservationService.createReservation(reservation);
        assertEquals(2, result);
    }

    @Test
    @DisplayName("时段冲突检测 - 无冲突")
    void testHasConflict_NoConflict() {
        User user = createTestUser("conflict_user_001");
        Equipment equipment = createTestEquipment("无冲突测试设备", 0);

        String startTime = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0).format(FORMATTER);
        String endTime = LocalDateTime.now().plusDays(3).withHour(11).withMinute(0).format(FORMATTER);

        boolean hasConflict = reservationService.hasConflict(equipment.getId(), startTime, endTime);
        assertFalse(hasConflict);
    }

    @Test
    @DisplayName("审批预约 - 成功场景")
    void testApproveReservation_Success() {
        User user = createTestUser("approve_user_001");
        Equipment equipment = createTestEquipment("审批测试设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(5).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0));
        reservation.setPurpose("审批测试");
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

        boolean result = reservationService.approveReservation(reservation.getId());
        assertTrue(result);

        Reservation updated = reservationMapper.selectById(reservation.getId());
        assertEquals(1, updated.getStatus());
    }

    @Test
    @DisplayName("审批预约 - 重复审批")
    void testApproveReservation_AlreadyProcessed() {
        User user = createTestUser("approve_user_002");
        Equipment equipment = createTestEquipment("重复审批设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(6).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(6).withHour(10).withMinute(0));
        reservation.setStatus(1);
        reservationMapper.insert(reservation);

        boolean result = reservationService.approveReservation(reservation.getId());
        assertFalse(result);
    }

    @Test
    @DisplayName("拒绝预约 - 成功场景")
    void testRejectReservation_Success() {
        User user = createTestUser("reject_user_001");
        Equipment equipment = createTestEquipment("拒绝测试设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(7).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0));
        reservation.setPurpose("拒绝测试");
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

        boolean result = reservationService.rejectReservation(reservation.getId(), "设备维修中");
        assertTrue(result);

        Reservation updated = reservationMapper.selectById(reservation.getId());
        assertEquals(2, updated.getStatus());
        assertEquals("设备维修中", updated.getRejectReason());
    }

    @Test
    @DisplayName("拒绝预约 - 非待审批状态")
    void testRejectReservation_NotPending() {
        User user = createTestUser("reject_user_002");
        Equipment equipment = createTestEquipment("非待审批设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(8).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(8).withHour(10).withMinute(0));
        reservation.setStatus(3);
        reservationMapper.insert(reservation);

        boolean result = reservationService.rejectReservation(reservation.getId(), "不应被拒绝");
        assertFalse(result);
    }

    @Test
    @DisplayName("取消预约 - 成功场景")
    void testCancelReservation_Success() {
        User user = createTestUser("cancel_user_001");
        Equipment equipment = createTestEquipment("取消测试设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(9).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(9).withHour(10).withMinute(0));
        reservation.setStatus(0);
        reservationMapper.insert(reservation);

        boolean result = reservationService.cancelReservation(reservation.getId());
        assertTrue(result);

        Reservation updated = reservationMapper.selectById(reservation.getId());
        assertEquals(3, updated.getStatus());
    }

    @Test
    @DisplayName("取消预约 - 已拒绝状态")
    void testCancelReservation_AlreadyRejected() {
        User user = createTestUser("cancel_user_002");
        Equipment equipment = createTestEquipment("已拒绝设备", 0);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEquipmentId(equipment.getId());
        reservation.setStartTime(LocalDateTime.now().plusDays(10).withHour(9).withMinute(0));
        reservation.setEndTime(LocalDateTime.now().plusDays(10).withHour(10).withMinute(0));
        reservation.setStatus(2);
        reservationMapper.insert(reservation);

        boolean result = reservationService.cancelReservation(reservation.getId());
        assertFalse(result);
    }

    @Test
    @DisplayName("用户预约列表查询 - 成功场景")
    void testGetUserReservations_Success() {
        User user = createTestUser("list_user_001");
        Equipment equipment = createTestEquipment("列表测试设备", 0);

        for (int i = 0; i < 3; i++) {
            Reservation reservation = new Reservation();
            reservation.setUserId(user.getId());
            reservation.setEquipmentId(equipment.getId());
            reservation.setStartTime(LocalDateTime.now().plusDays(10 + i).withHour(9).withMinute(0));
            reservation.setEndTime(LocalDateTime.now().plusDays(10 + i).withHour(10).withMinute(0));
            reservation.setStatus(0);
            reservationMapper.insert(reservation);
        }

        List<Map<String, Object>> reservations = reservationService.getUserReservations(user.getId());
        assertNotNull(reservations);
        assertTrue(reservations.size() >= 3);
    }

    @Test
    @DisplayName("待审批预约列表查询 - 成功场景")
    void testGetPendingReservations_Success() {
        User user = createTestUser("pending_user_001");
        Equipment equipment = createTestEquipment("待审批列表设备", 0);

        Reservation reservation1 = new Reservation();
        reservation1.setUserId(user.getId());
        reservation1.setEquipmentId(equipment.getId());
        reservation1.setStartTime(LocalDateTime.now().plusDays(11).withHour(9).withMinute(0));
        reservation1.setEndTime(LocalDateTime.now().plusDays(11).withHour(10).withMinute(0));
        reservation1.setStatus(0);
        reservationMapper.insert(reservation1);

        List<Map<String, Object>> pendingList = reservationService.getPendingReservations();
        assertNotNull(pendingList);
        assertTrue(pendingList.stream()
                .anyMatch(r -> r.get("userName").equals("测试用户")));
    }

    @Test
    @DisplayName("可用时段查询 - 成功场景")
    void testGetAvailableSlots_Success() {
        User user = createTestUser("slots_user_001");
        Equipment equipment = createTestEquipment("时段可用设备", 0);

        String date = LocalDateTime.now().plusDays(12).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> slots = reservationService.getAvailableSlots(equipment.getId(), date);
        assertNotNull(slots);
        assertFalse(slots.isEmpty());
    }
}
