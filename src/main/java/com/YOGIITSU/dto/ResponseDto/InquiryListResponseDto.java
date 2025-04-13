package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.entity.Inquiry;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class InquiryListResponseDto {
    private final String inquiryTitle;
    private final String authorName;
    private final String inquiryState;
    private final LocalDateTime inquiryAt;

    public InquiryListResponseDto(Inquiry inquiry) {
        this.inquiryTitle = inquiry.getInquiryTitle();
        this.authorName = inquiry.getMember().getUsername();
        this.inquiryState = inquiry.getInquiryState().name();  // "PROCESSING", "COMPLETED"
        this.inquiryAt = inquiry.getInquiryAt();
    }
}
