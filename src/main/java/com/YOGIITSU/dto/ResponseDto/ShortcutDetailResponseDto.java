package com.YOGIITSU.dto.ResponseDto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShortcutDetailResponseDto {

    private Long shortcutId;
    private String pointA;
    private String pointB;
    private Double distance;
    private List<CoordinateDto> coordinates;

}
