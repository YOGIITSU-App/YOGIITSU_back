package com.YOGIITSU.config.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler { //클라이언트에 적절한 에러 메시지를 반환하는 역할

    /*
    유효성 검사 실패 예외를 처리한다
    DTO 클래스에서 @NotBlank, @Size, @Email 등으로 정의된 유효성 검사가 실패했을 때 발생함
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body("입력값이 유효하지 않습니다: " + e.getMessage());
        // 클라이언트에게 "입력값이 유효하지 않습니다" 메시지와 예외 메시지를 반환
    }

    @ExceptionHandler(IllegalArgumentException.class) // IllegalArgumentException 발생 시 호출
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}