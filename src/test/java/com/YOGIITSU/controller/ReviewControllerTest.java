package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.ReviewCreateRequestDto;
import com.YOGIITSU.dto.ResponseDto.PaginationInfo;
import com.YOGIITSU.dto.ResponseDto.ReviewPageResponseDto;
import com.YOGIITSU.dto.ResponseDto.ReviewResponseDto;
import com.YOGIITSU.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ReviewService reviewService;

	@Test
	@DisplayName("리뷰 목록 조회 성공")
	void getAllReviews_success() throws Exception {
		// given
		ReviewPageResponseDto response = ReviewPageResponseDto.builder()
			.reviews(List.of(mock(ReviewResponseDto.class)))
			.pagination(
				PaginationInfo.builder()
					.page(1)
					.limit(6)
					.totalPages(1)
					.totalCount(1)
					.build()
			)
			.build();

		when(reviewService.getReviewsWithPagination(1, 6)).thenReturn(response);

		// when & then
		mockMvc.perform(get("/reviews")
				.param("page", "1")
				.param("limit", "6"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reviews").isArray())
			.andExpect(jsonPath("$.pagination.page").value(1));
	}

	@Test
	@DisplayName("page, limit 음수일 경우 기본값 적용")
	void getAllReviews_defaultValue() throws Exception {
		when(reviewService.getReviewsWithPagination(1, 6))
			.thenReturn(mock(ReviewPageResponseDto.class));

		mockMvc.perform(get("/reviews")
				.param("page", "-1")
				.param("limit", "-1"))
			.andExpect(status().isOk());

		verify(reviewService).getReviewsWithPagination(1, 6);
	}

	@Test
	@DisplayName("리뷰 생성 성공")
	void createReview_success() throws Exception {
		// given
		ReviewCreateRequestDto request = ReviewCreateRequestDto.builder()
			.title("요기있수 최고")
			.content("요기있수 최고입니다")
			.department("컴퓨터SW학과")
			.studentId("21학번")
			.name("박소미")
			.build();

		ReviewResponseDto response = mock(ReviewResponseDto.class);
		when(reviewService.createReview(any())).thenReturn(response);

		// when & then
		mockMvc.perform(post("/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("리뷰 생성 실패 - 필수값 누락")
	void createReview_validationFail() throws Exception {
		ReviewCreateRequestDto request = ReviewCreateRequestDto.builder()
			.title("") // NotBlank 위반
			.build();

		mockMvc.perform(post("/reviews")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}


	@Test
	@DisplayName("리뷰 삭제 성공")
	void deleteReview_success() throws Exception {
		doNothing().when(reviewService).deleteReview(1L);

		mockMvc.perform(delete("/reviews/{id}", 1L))
			.andExpect(status().isNoContent());
	}
}
