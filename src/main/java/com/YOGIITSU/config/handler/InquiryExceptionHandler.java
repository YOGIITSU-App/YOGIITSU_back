package com.YOGIITSU.config.handler;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

// Inquiry 컨트롤러 관련 예외만 처리
@ControllerAdvice(basePackages = "com.YOGIITSU.controller")
public class InquiryExceptionHandler {

    /**
     * 잘못된 요청 예외 처리 (400 Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest() {
        return ResponseEntity.badRequest().body(Map.of("message", "잘못된 요청입니다."));
    }

    /**
     * 리소스를 찾을 수 없는 예외 처리 (404 Not Found)
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNullPointer() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "요청한 리소스를 찾을 수 없습니다."));
    }

    /**
     * 런타임 예외 처리 (500 Internal Server Error)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "서버 오류가 발생했습니다."));
    }
}
