package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.ReviewCreateRequestDto;
import com.YOGIITSU.dto.ResponseDto.ReviewResponseDto;
import com.YOGIITSU.entity.Review;
import com.YOGIITSU.exception.resource.ReviewNotFoundException;
import com.YOGIITSU.repository.ReviewRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
