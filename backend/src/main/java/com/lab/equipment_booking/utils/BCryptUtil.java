package com.lab.equipment_booking.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encode(String raw) {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return encoder.encode(raw);
    }

    public static boolean matches(String raw, String encoded) {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (encoded == null || encoded.isEmpty()) {
            throw new IllegalArgumentException("加密密码不能为空");
        }
        return encoder.matches(raw, encoded);
    }

    public static void main(String[] args) {
        System.out.println(encode("123456"));
    }
}