package com.YOGIITSU.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InquiryRequestDto {
    // 문의 내용 작성 시 필요한 데이터 받기 위함
    private String inquiryContent;  // 문의 내용
    private String passwordHash;  // 비밀번호 검증용
}
