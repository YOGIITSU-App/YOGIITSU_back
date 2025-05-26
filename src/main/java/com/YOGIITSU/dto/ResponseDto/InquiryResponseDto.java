package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.entity.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문의 상세 응답 DTO
 * - 제목, 내용, 답변, 작성일, 답변일 등 전체 정보 포함
 */
@Getter
@AllArgsConstructor
public class InquiryResponseDto {

    private Long inquiryId;
    private String inquiryTitle;
    private String inquiryContent;
    private LocalDateTime inquiryAt;

    private Long authorId;
    private String authorName;

    private String response;
    private LocalDateTime responseAt;

    public InquiryResponseDto(Inquiry inquiry) {
        this.inquiryId = inquiry.getInquiryId();
        this.inquiryTitle = inquiry.getInquiryTitle();
        this.inquiryContent = inquiry.getInquiryContent();
        this.authorId = inquiry.getMember().getId();
        this.authorName = inquiry.getMember().getUserName();
        this.inquiryAt = inquiry.getInquiryAt();
        this.response = inquiry.getResponse();
        this.responseAt = inquiry.getResponseAt();
    }
}
