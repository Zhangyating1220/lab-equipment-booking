package com.lab.equipment_booking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lab.equipment_booking.mapper")
public class EquipmentBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(EquipmentBookingApplication.class, args);
	}

}