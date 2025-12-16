package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.ReviewCreateRequestDto;
import com.YOGIITSU.dto.ResponseDto.PaginationInfo;
import com.YOGIITSU.dto.ResponseDto.ReviewPageResponseDto;
import com.YOGIITSU.dto.ResponseDto.ReviewResponseDto;
import com.YOGIITSU.entity.Review;
import com.YOGIITSU.exception.resource.ReviewNotFoundException;
import com.YOGIITSU.repository.ReviewRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

	private final ReviewRepository reviewRepository;

	public List<ReviewResponseDto> getAllReviews() {

		return reviewRepository.findAllByOrderByCreatedAtDesc().stream()
			.map(ReviewResponseDto::from)
			.collect(Collectors.toList());
	}

	public ReviewPageResponseDto getReviewsWithPagination(int page, int limit) {
		Pageable pageable = PageRequest.of(page - 1, limit);
		Page<Review> reviewPage = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);

		List<ReviewResponseDto> reviews = reviewPage.getContent().stream()
			.map(ReviewResponseDto::from)
			.collect(Collectors.toList());

		PaginationInfo pagination = PaginationInfo.builder()
			.page(page)
			.totalPages(reviewPage.getTotalPages())
			.totalCount(reviewPage.getTotalElements())
			.limit(limit)
			.build();

		return ReviewPageResponseDto.builder()
			.reviews(reviews)
			.pagination(pagination)
			.build();
	}

	@Transactional
	public ReviewResponseDto createReview(ReviewCreateRequestDto request) {

		Review review = request.toEntity();
		Review savedReview = reviewRepository.save(review);

		return ReviewResponseDto.from(savedReview);
	}

	@Transactional
	public void deleteReview(Long id) {

		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new ReviewNotFoundException(id));

		reviewRepository.delete(review);
	}
}
