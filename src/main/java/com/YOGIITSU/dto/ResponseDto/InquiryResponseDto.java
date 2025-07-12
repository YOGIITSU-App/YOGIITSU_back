package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.entity.Inquiry;
import com.YOGIITSU.entity.InquiryState;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 문의 상세 응답 DTO
 * - 제목, 내용, 답변 내용, 작성일, 답변일 등 전체 정보 포함
 */
@Getter
@AllArgsConstructor
public class InquiryResponseDto {

    private Long inquiryId;
    private String inquiryTitle;
    private String inquiryContent;
    private LocalDateTime inquiryAt;
    private InquiryState inquiryState;

    private Long authorId;
    private String authorName;

    private String answerTitle;
    private String answerContent;
    private LocalDateTime answerAt;

    public InquiryResponseDto(Inquiry inquiry) {

        this.inquiryId = inquiry.getInquiryId();
        this.inquiryTitle = inquiry.getInquiryTitle();
        this.inquiryContent = inquiry.getInquiryContent();
        this.inquiryAt = inquiry.getInquiryAt();
        this.inquiryState = inquiry.getInquiryState();
        this.authorId = inquiry.getMember().getId();
        this.authorName = inquiry.getMember().getUserName();
        this.answerTitle = inquiry.getAnswerTitle();
        this.answerContent = inquiry.getAnswerContent();
        this.answerAt = inquiry.getAnswerAt();
    }
}
