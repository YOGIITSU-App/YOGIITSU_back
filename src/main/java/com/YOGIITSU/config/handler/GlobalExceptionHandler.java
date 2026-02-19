package com.YOGIITSU.config.handler;

import com.YOGIITSU.dto.ErrorResponse;
import com.YOGIITSU.exception.BaseException;
import com.YOGIITSU.exception.ErrorCode;
import com.YOGIITSU.exception.external.AppleExchangeException;
import com.YOGIITSU.exception.auth.*;
import com.YOGIITSU.exception.building.BuildingNotFoundException;
import com.YOGIITSU.exception.cafeteria.CafeteriaNotFoundForBuildingException;
import com.YOGIITSU.exception.user.*;
import com.YOGIITSU.exception.resource.ResourceException;
import com.YOGIITSU.exception.resource.NoticeNotFoundException;
import com.YOGIITSU.exception.resource.InquiryNotFoundException;
import com.YOGIITSU.exception.resource.FavoriteNotFoundException;
import com.YOGIITSU.exception.resource.ReviewNotFoundException;
import com.YOGIITSU.exception.validation.EmailAlreadyExistsException;
import com.YOGIITSU.exception.validation.UsernameAlreadyExistsException;
import com.YOGIITSU.exception.validation.EmailRequiredException;
import com.YOGIITSU.exception.external.ExternalServiceException;
import com.YOGIITSU.exception.external.GoogleAuthException;
import com.YOGIITSU.exception.external.KakaoAuthException;
import com.YOGIITSU.exception.external.EmailSendException;
import com.YOGIITSU.exception.validation.ValidationException;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.exception.validation.MissingRequiredFieldException;
import com.YOGIITSU.exception.system.SystemException;
import com.YOGIITSU.exception.system.DatabaseException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;


/**
 * 전역 예외 처리 핸들러
 * 모든 예외를 일관된 형식으로 처리하여 클라이언트에게 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외 처리 (BaseException을 상속받은 모든 예외)
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        log.warn("Custom exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getDetailMessage()));
    }

    /**
     * 인증 관련 예외 처리
     */
    @ExceptionHandler({
            UnauthorizedException.class,
            InvalidTokenException.class,
            MissingTokenException.class,
            InvalidLoginException.class,
            EmailVerificationNotApprovedException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(BaseException e) {
        log.warn("Authentication exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getDetailMessage()));
    }

    /**
     * 권한 관련 예외 처리
     */
    @ExceptionHandler({
            AccessDeniedException.class,
            AdminAccessDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthorizationException(BaseException e) {
        log.warn("Authorization exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getDetailMessage()));
    }

    /**
     * 사용자 관련 예외 처리
     */
    @ExceptionHandler({
            MemberNotFoundException.class,
            MemberAlreadyExistsException.class,
            PasswordMismatchException.class,
            PasswordNotEqualsException.class,
            SamePasswordException.class,
            SameEmailException.class,
            EmailNotRegisteredException.class
    })
    public ResponseEntity<ErrorResponse> handleUserException(BaseException e) {
        log.warn("User exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getDetailMessage()));
    }

    /**
     * 리소스 관련 예외 처리
     */
    @ExceptionHandler({
            BuildingNotFoundException.class,
            CafeteriaNotFoundForBuildingException.class,
            NoticeNotFoundException.class,
            InquiryNotFoundException.class,
            FavoriteNotFoundException.class,
            ReviewNotFoundException.class,
            ResourceException.class
    })
    public ResponseEntity<ErrorResponse> handleResourceException(BaseException e) {
        log.warn("Resource exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getDetailMessage()));
    }

    /**
     * 외부 서비스 관련 예외 처리
     */
    @ExceptionHandler({
            AppleExchangeException.class,
            GoogleAuthException.class,
            KakaoAuthException.class,
            EmailSendException.class,
            ExternalServiceException.class
    })
    public ResponseEntity<ErrorResponse> handleExternalServiceException(BaseException e) {
        log.warn("External service exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getDetailMessage()));
    }

    /**
     * Apple 인증 서비스 예외 처리
     */
    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleAppleAuthServiceException(AuthenticationServiceException e) {
        log.warn("Apple authentication service exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(ErrorCode.APPLE_AUTH_FAIL, null));
    }

    /**
     * JPA EntityNotFoundException 처리
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("Entity not found exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.MEMBER_NOT_FOUND, null));
    }

    /**
     * JSON 파싱 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("JSON parsing exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, "요청 형식이 올바르지 않습니다. JSON 형식을 확인해주세요."));
    }

    /**
     * 유효성 검사 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation exception occurred: {}", e.getMessage(), e);

        // 안전한 오류 메시지 추출
        String errorMessage = "요청 값이 유효하지 않습니다.";
        if (e.getBindingResult() != null && e.getBindingResult().hasErrors()) {
            var errors = e.getBindingResult().getAllErrors();
            if (!errors.isEmpty()) {
                var firstError = errors.get(0);
                String fieldName = "";
                if (firstError instanceof org.springframework.validation.FieldError fieldError) {
                    fieldName = fieldError.getField() + ": ";
                }
                errorMessage = fieldName + firstError.getDefaultMessage();
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, errorMessage));
    }

    /**
     * 유효성 검사 관련 예외 처리
     */
    @ExceptionHandler({
            ValidationException.class,
            MissingRequiredFieldException.class,
            EmailAlreadyExistsException.class,
            UsernameAlreadyExistsException.class,
            EmailRequiredException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException(BaseException e) {
        log.warn("Validation exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getDetailMessage()));
    }

    /**
     * 요청 파라미터 관련 예외 처리
     */
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleRequestParameterException(Exception e) {
        log.warn("Request parameter exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_ARGUMENT, "요청 파라미터가 올바르지 않습니다."));
    }

    /**
     * 잘못된 인수 예외 처리
     */
    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgumentException(InvalidArgumentException e) {
        log.warn("Invalid argument exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), null));
    }

    /**
     * 잘못된 인수 예외 처리 (기존 IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_ARGUMENT, null));
    }

    /**
     * 이메일 전송 실패 예외 처리
     */
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ErrorResponse> handleMailException(MailException e) {
        log.error("Mail exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.EMAIL_SEND_FAIL, null));
    }

    /**
     * 시스템 관련 예외 처리
     */
    @ExceptionHandler({
            SystemException.class,
            DatabaseException.class
    })
    public ResponseEntity<ErrorResponse> handleSystemException(BaseException e) {
        log.error("System exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getDetailMessage()));
    }

    /**
     * 기타 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, null));
    }

    /**
     * 예상치 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }
}