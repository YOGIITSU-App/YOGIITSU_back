package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppVersionResponseDto {

	private String updateType; // "NONE", "SELECT", "FORCE"
	private String currentVersion;
	private String minVersion;
	private String latestVersion;
}
