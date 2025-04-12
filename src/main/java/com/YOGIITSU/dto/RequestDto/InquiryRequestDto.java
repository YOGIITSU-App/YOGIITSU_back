package com.YOGIITSU.dto.RequestDto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InquiryRequestDto {
    // 문의 내용 작성 시 필요한 데이터 받기 위함
    private String inquiryTitle;  // 문의 제목
    private String inquiryContent;  // 문의 내용

    @Size(min = 4, max = 20, message = "비밀번호는 4 ~ 20자의 영문 또는 숫자만 입력하세요.")
    private String inquiryPassword;  // 사용자 검증용 비밀번호
}
