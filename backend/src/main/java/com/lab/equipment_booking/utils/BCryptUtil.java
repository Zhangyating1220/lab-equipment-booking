package com.lab.equipment_booking.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptUtil {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encode(String raw) {
        return encoder.encode(raw);
    }

    public static boolean matches(String raw, String encoded) {
        return encoder.matches(raw, encoded);
    }

    public static void main(String[] args) {
        System.out.println(encode("123456"));
    }
}