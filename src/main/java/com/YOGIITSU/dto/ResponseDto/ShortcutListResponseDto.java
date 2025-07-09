package com.YOGIITSU.dto.ResponseDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShortcutListResponseDto {

    private Long shortcutId;
    private String pointA;
    private String pointB;
    private Double distance;
    private Integer duration;

}
