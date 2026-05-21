package com.lab.equipment_booking.exception;

import com.lab.equipment_booking.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.ok(Result.error(e.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数异常: {}", e.getMessage());
        return ResponseEntity.ok(Result.error(400, e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        logger.error("系统异常", e);
        return ResponseEntity.ok(Result.error(500, "系统繁忙，请稍后重试"));
    }
}