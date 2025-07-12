package com.YOGIITSU.dto.RequestDto;

import lombok.Getter;

/**
 * 관리자 답변 수정용 DTO
 * - 제목 또는 내용 중 하나 이상 입력
 * - 유효성 검사는 서비스 단에서 수동 처리
 */
@Getter
public class AdminAnswerUpdateRequestDto {

    private String answerTitle;
    private String answerContent;

}
