package com.lab.equipment_booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lab.equipment_booking.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
}