package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.entity.Inquiry;
import lombok.Getter;

@Getter
public class InquiryListResponseDto {
    private final Long inquiryId;
    private final String inquiryTitle;
    private final String authorName;

    public InquiryListResponseDto(Inquiry inquiry) {
        this.inquiryId = inquiry.getInquiryId();
        this.inquiryTitle = inquiry.getInquiryTitle();
        this.authorName = inquiry.getMember().getUsername();
    }
}
