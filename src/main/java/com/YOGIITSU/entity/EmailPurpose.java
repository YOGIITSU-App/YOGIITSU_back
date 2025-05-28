package com.YOGIITSU.entity;

import lombok.Getter;

// 목적
@Getter
public enum EmailPurpose {
    SIGNUP("이메일 인증이 완료되었습니다. 회원가입을 계속 진행해주세요."), // 회원가입
    EMAIL_CHANGE_OLD("이메일 인증이 완료되었습니다."), // 이메일 변경 - 기존 이메일 확인
    EMAIL_CHANGE_NEW("이메일이 변경되었습니다. 보안을 위해 다시 로그인해주세요."), // 이메일 변경 - 새 이메일 확인
    PASSWORD_CHANGE("이메일 인증이 완료되었습니다. 비밀번호 재설정을 계속 진행해주세요."), // 비밀번호 변경
    FIND_ID("이메일 인증이 완료되었습니다. 아이디 찾기를 계속 진행해주세요."), // 아이디 찾기
    FIND_PASSWORD("이메일 인증이 완료되었습니다. 비밀번호 재설정을 계속 진행해주세요."); // 비밀번호 찾기

    private final String successMessage;

    EmailPurpose(String successMessage) {
        this.successMessage = successMessage;
    }

    public static boolean requiresLogin(EmailPurpose purpose) {
        return switch (purpose) {
            case EMAIL_CHANGE_OLD, EMAIL_CHANGE_NEW, PASSWORD_CHANGE, FIND_ID, FIND_PASSWORD ->
                true;
            default -> false;
        };
    }
}