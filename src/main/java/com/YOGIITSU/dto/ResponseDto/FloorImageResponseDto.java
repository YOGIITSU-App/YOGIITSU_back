package com.YOGIITSU.dto.ResponseDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FloorImageResponseDto {

	private String floor;

	private String imageUrl;
}
