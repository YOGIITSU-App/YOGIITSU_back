package com.YOGIITSU.dto.ResponseDto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FavoriteListResponseDto {

	private String memberId;
	private List<BuildingResponseDto> buildings;
}
