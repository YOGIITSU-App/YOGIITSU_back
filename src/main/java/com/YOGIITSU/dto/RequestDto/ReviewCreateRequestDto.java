package com.YOGIITSU.dto.RequestDto;

import com.YOGIITSU.entity.Review;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequestDto {

	@NotBlank(message = "제목은 필수입니다")
	@Size(max = 100, message = "제목은 100자 이내여야 합니다")
	private String title;

	@NotBlank(message = "내용은 필수입니다")
	private String content;

	@NotBlank(message = "학과는 필수입니다")
	@Size(max = 50, message = "학과명은 50자 이내여야 합니다")
	private String department;

	@NotBlank(message = "학번은 필수입니다")
	@Size(max = 20, message = "학번은 20자 이내여야 합니다")
	private String studentId;

	@NotBlank(message = "이름은 필수입니다")
	@Size(max = 50, message = "이름은 50자 이내여야 합니다")
	private String name;

	public Review toEntity() {
		return Review.builder()
			.title(title)
			.content(content)
			.department(department)
			.studentId(studentId)
			.name(name)
			.build();
	}
}