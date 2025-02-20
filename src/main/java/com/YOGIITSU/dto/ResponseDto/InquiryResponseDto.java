package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InquiryResponseDto {
    // 사용자에게 반환할 데이터 담는 dto
    private Long inquiryId;
    private String inquiryTitle;
    private String inquiryContent;
    private String response;
    private LocalDateTime inquiryAt;
    private LocalDateTime responseAt;

    public InquiryResponseDto(Inquiry inquiry) {
        // Inquiry 엔티티 기반으로 필요한 필드만 dto로 변환
        this.inquiryId = inquiry.getInquiryId();
        this.inquiryTitle = inquiry.getInquiryTitle();
        this.inquiryContent = inquiry.getInquiryContent();
        this.response = inquiry.getResponse();
        this.inquiryAt = inquiry.getInquiryAt();
        this.responseAt = inquiry.getResponseAt();
    }
}
