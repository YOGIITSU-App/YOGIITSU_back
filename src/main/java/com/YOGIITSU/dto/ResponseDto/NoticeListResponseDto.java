package com.YOGIITSU.dto.ResponseDto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NoticeListResponseDto { // 공지사항 전체 조회

    private Long noticeId;                // 공지사항 ID
    private String noticeTitle;           // 공지사항 제목
    private LocalDateTime noticeAt; // 작성일시
}