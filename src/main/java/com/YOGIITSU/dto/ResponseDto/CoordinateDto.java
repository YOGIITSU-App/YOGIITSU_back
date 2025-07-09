package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.enums.TurnType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoordinateDto {

    private Double latitude;
    private Double longitude;
    private Integer pointOrder;
    private String description;
    private TurnType turnType;
    private Double segmentDistance;
    private String imageUrl;

}
