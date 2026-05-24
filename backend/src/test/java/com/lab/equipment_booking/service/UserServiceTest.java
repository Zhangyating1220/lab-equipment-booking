package com.lab.equipment_booking.service;

import com.lab.equipment_booking.entity.User;
import com.lab.equipment_booking.mapper.UserMapper;
import com.lab.equipment_booking.service.impl.UserServiceImpl;
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
    void testFindByUsername_NotFound() {
        User foundUser = userService.findByUsername("nonexistent");
        assertNull(foundUser);
    }

    @Test
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

        assertThrows(Exception.class, () -> userService.register(user2));
    }

    @Test
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
}
