package com.YOGIITSU.dto.ResponseDto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPageResponseDto {

	private List<ReviewResponseDto> reviews;
	private PaginationInfo pagination;
}

