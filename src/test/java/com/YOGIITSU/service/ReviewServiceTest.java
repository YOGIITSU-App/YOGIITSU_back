package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.ReviewCreateRequestDto;
import com.YOGIITSU.dto.ResponseDto.ReviewPageResponseDto;
import com.YOGIITSU.dto.ResponseDto.ReviewResponseDto;
import com.YOGIITSU.entity.Review;
import com.YOGIITSU.exception.resource.ReviewNotFoundException;
import com.YOGIITSU.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@InjectMocks
	private ReviewService reviewService;

	@Test
	@DisplayName("리뷰 페이징 조회 성공")
	void getReviewsWithPagination_success() {
		// given
		Review review = mock(Review.class);
		Page<Review> page = new PageImpl<>(
			List.of(review),
			PageRequest.of(0, 6),
			1
		);

		when(reviewRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
			.thenReturn(page);

		// when
		ReviewPageResponseDto result = reviewService.getReviewsWithPagination(1, 6);

		// then
		assertThat(result.getReviews()).hasSize(1);
		assertThat(result.getPagination().getPage()).isEqualTo(1);
		assertThat(result.getPagination().getTotalPages()).isEqualTo(1);
		assertThat(result.getPagination().getTotalCount()).isEqualTo(1);
		assertThat(result.getPagination().getLimit()).isEqualTo(6);
	}

	@Test
	@DisplayName("리뷰 전체 조회 성공 (최신순)")
	void getAllReviews_success() {
		// given
		Review review1 = mock(Review.class);
		Review review2 = mock(Review.class);

		when(reviewRepository.findAllByOrderByCreatedAtDesc())
			.thenReturn(List.of(review1, review2));

		// when
		List<ReviewResponseDto> result = reviewService.getAllReviews();

		// then
		assertThat(result).hasSize(2);
		verify(reviewRepository).findAllByOrderByCreatedAtDesc();
	}


	@Test
	@DisplayName("리뷰 생성 성공")
	void createReview_success() {
		// given
		ReviewCreateRequestDto request = mock(ReviewCreateRequestDto.class);
		Review review = mock(Review.class);

		when(request.toEntity()).thenReturn(review);
		when(reviewRepository.save(review)).thenReturn(review);

		// when
		ReviewResponseDto result = reviewService.createReview(request);

		// then
		assertThat(result).isNotNull();
		verify(reviewRepository).save(review);
	}

	@Test
	@DisplayName("리뷰 삭제 성공")
	void deleteReview_success() {
		// given
		Long reviewId = 1L;
		Review review = mock(Review.class);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		// when
		reviewService.deleteReview(reviewId);

		// then
		verify(reviewRepository).delete(review);
	}

	@Test
	@DisplayName("존재하지 않는 리뷰 삭제 시 예외 발생")
	void deleteReview_notFound() {
		// given
		Long reviewId = 1L;
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		// then
		assertThatThrownBy(() -> reviewService.deleteReview(reviewId))
			.isInstanceOf(ReviewNotFoundException.class);
	}
}
