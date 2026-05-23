package com.lab.equipment_booking.config;

import com.lab.equipment_booking.service.UsageRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);
    
    @Autowired
    private UsageRecordService usageRecordService;

    @Scheduled(cron = "0 * * * * ?")
    public void releaseTimeoutRecords() {
        logger.info("开始执行超时使用记录释放任务");
        usageRecordService.releaseTimeoutRecords();
        logger.info("超时使用记录释放任务执行完成");
    }
}