package com.YOGIITSU.dto.ResponseDto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShortcutDetailResponse {

    private Long shortcutId;
    private String shortcutName;
    private String startPoint;
    private String endPoint;
    private String description;
    private Double distance;
    private String imageUrl;
    private List<CoordinateDto> coordinates;

}
