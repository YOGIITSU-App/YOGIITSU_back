package com.YOGIITSU.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InquiryRequestDto {
    // 문의 내용 작성 시 필요한 데이터 받기 위함
    private String inquiryTitle;  // 문의 제목
    private String inquiryContent;  // 문의 내용
    private String password;  // 사용자 검증용 비밀번호
}
