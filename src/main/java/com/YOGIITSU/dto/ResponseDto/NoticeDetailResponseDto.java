package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NoticeDetailResponseDto { // 공지사항 상세 조회

    private Long noticeId;
    private String noticeTitle;
    private String noticeContent;
    private LocalDateTime noticeAt;
}