package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationInfo {

	private int page;
	private int totalPages;
	private long totalCount;
	private int limit;
}

