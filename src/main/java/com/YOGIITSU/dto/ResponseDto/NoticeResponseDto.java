package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NoticeResponseDto {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
