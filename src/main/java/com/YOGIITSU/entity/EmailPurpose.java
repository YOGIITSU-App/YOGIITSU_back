package com.YOGIITSU.entity;

// 이메일 변경 목적
public enum EmailPurpose {
    SIGNUP, // 회원가입 이메일 전송
    NORMAL, // 이메일 전송해야 할 때(예를 들어, 이메일 변경 전 기존 이메일 전송할 때 등..)
    EMAIL_CHANGE // 이메일 변경할 때 이메일 전송
}
