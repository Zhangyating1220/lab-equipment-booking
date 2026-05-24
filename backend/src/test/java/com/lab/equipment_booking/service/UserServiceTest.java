package com.lab.equipment_booking.service;

import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.exception.BusinessException;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("根据用户名查找用户-成功")
    void testFindByUsername_Success() {
        User user = new User();
        user.setUsername("testuser001");
        user.setPassword("password123");
        user.setName("测试用户");
        user.setRole(0);
        userService.register(user);

        User foundUser = userService.findByUsername("testuser001");
        assertNotNull(foundUser);
        assertEquals("测试用户", foundUser.getName());
        assertEquals(0, foundUser.getRole());
    }

    @Test
    @DisplayName("根据用户名查找用户-不存在")
    void testFindByUsername_NotFound() {
        User foundUser = userService.findByUsername("nonexistent");
        assertNull(foundUser);
    }

    @Test
    @DisplayName("用户注册-成功")
    void testRegister_Success() {
        User user = new User();
        user.setUsername("newuser001");
        user.setPassword("password123");
        user.setName("新用户");
        user.setRole(0);

        boolean result = userService.register(user);
        assertTrue(result);

        User registeredUser = userMapper.findByUsername("newuser001");
        assertNotNull(registeredUser);
        assertEquals("新用户", registeredUser.getName());
    }

    @Test
    @DisplayName("用户注册-用户名重复")
    void testRegister_DuplicateUsername() {
        User user1 = new User();
        user1.setUsername("duplicate001");
        user1.setPassword("password123");
        user1.setName("用户1");
        user1.setRole(0);
        userService.register(user1);

        User user2 = new User();
        user2.setUsername("duplicate001");
        user2.setPassword("password456");
        user2.setName("用户2");
        user2.setRole(0);

        assertThrows(BusinessException.class, () -> userService.register(user2));
    }

    @Test
    @DisplayName("用户注册-密码加密存储")
    void testRegister_PasswordEncrypted() {
        User user = new User();
        user.setUsername("encrypttest");
        user.setPassword("plainpassword");
        user.setName("加密测试");
        user.setRole(0);

        userService.register(user);

        User registeredUser = userMapper.findByUsername("encrypttest");
        assertNotNull(registeredUser);
        assertNotEquals("plainpassword", registeredUser.getPassword());
    }

    @Test
    @DisplayName("用户注册-管理员角色")
    void testRegister_AdminRole() {
        User user = new User();
        user.setUsername("admin001");
        user.setPassword("adminpass");
        user.setName("管理员");
        user.setRole(1);

        boolean result = userService.register(user);
        assertTrue(result);

        User admin = userMapper.findByUsername("admin001");
        assertNotNull(admin);
        assertEquals(1, admin.getRole());
    }
}