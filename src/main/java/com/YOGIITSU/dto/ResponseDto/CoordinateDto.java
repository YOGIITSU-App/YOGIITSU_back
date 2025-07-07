package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoordinateDto {

    private Double latitude;
    private Double longitude;
    private Integer pointOrder;
    private String description;
    private String imageUrl;

}
