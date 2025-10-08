package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.entity.Inquiry;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 문의 목록 응답 DTO
 * - 제목, 작성자 이름, 상태, 작성일, 본인 여부 포함
 */
@Getter
public class InquiryListResponseDto {

    private Long inquiryId;
    private String inquiryTitle;
    private String authorName;
    private String inquiryState;
    private LocalDateTime inquiryAt;
    @JsonProperty("isMine")
    private boolean isMine;

    public InquiryListResponseDto(Inquiry inquiry, boolean isMine) {
        this.inquiryId = inquiry.getInquiryId();
        this.inquiryTitle = inquiry.getInquiryTitle();
        this.authorName = inquiry.getMember().getUserName();
        this.inquiryState = inquiry.getInquiryState().name();  // "PROCESSING", "COMPLETED"
        this.inquiryAt = inquiry.getInquiryAt();
        this.isMine = isMine;
    }
}
