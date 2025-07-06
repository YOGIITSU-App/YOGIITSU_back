package com.YOGIITSU.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeRequestDto {

    @NotBlank(message = "공지 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "공지 내용은 필수입니다.")
    private String content;
}
