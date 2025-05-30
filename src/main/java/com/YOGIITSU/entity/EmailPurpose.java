package com.YOGIITSU.entity;

import lombok.Getter;

// 목적
@Getter
public enum EmailPurpose {
    // DB에 없어야 함 (false)
    SIGNUP(false, "이메일 인증이 완료되었습니다. 회원가입을 계속 진행해주세요."),
    EMAIL_CHANGE_NEW(false, "이메일이 변경되었습니다. 보안을 위해 다시 로그인해주세요."),

    // DB에 반드시 있어야 함 (true)
    EMAIL_CHANGE_OLD(true, "이메일 인증이 완료되었습니다."),
    PASSWORD_CHANGE(true, "이메일 인증이 완료되었습니다. 비밀번호 재설정을 계속 진행해주세요."),
    FIND_ID(true, "이메일 인증이 완료되었습니다. 아이디 찾기를 계속 진행해주세요."),
    FIND_PASSWORD(true, "이메일 인증이 완료되었습니다. 비밀번호 재설정을 계속 진행해주세요.");

    private final boolean mustExist;
    private final String successMessage;

    EmailPurpose(boolean mustExist, String successMessage) {
        this.mustExist = mustExist;
        this.successMessage = successMessage;
    }
}