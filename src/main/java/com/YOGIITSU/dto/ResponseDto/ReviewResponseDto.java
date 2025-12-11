package com.YOGIITSU.dto.ResponseDto;

import com.YOGIITSU.entity.Review;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {

	private Long id;
	private String title;
	private String content;
	private String department;
	private String studentId;
	private String name;
	private LocalDateTime createdAt;

	public static ReviewResponseDto from(Review review) {
		return ReviewResponseDto.builder()
			.id(review.getId())
			.title(review.getTitle())
			.content(review.getContent())
			.department(review.getDepartment())
			.studentId(review.getStudentId())
			.name(review.getName())
			.createdAt(review.getCreatedAt())
			.build();
	}
}