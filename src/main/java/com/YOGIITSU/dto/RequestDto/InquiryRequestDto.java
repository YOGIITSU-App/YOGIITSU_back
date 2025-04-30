package com.YOGIITSU.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 문의 등록 요청 DTO
 * - 사용자가 작성한 제목, 내용을 전달 받음
 */
@Getter
@AllArgsConstructor
public class InquiryRequestDto {

    private String inquiryTitle;  // 문의 제목
    private String inquiryContent;  // 문의 내용

}
