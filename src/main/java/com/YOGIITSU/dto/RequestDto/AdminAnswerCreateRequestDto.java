package com.YOGIITSU.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AdminAnswerCreateRequestDto {

    @NotBlank(message = "답변 제목을 입력해주세요.")
    private String answerTitle;

    @NotBlank(message = "답변 내용을 입력해주세요.")
    private String answerContent;
}
