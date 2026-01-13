package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.ReviewCreateRequestDto;
import com.YOGIITSU.dto.ResponseDto.ReviewPageResponseDto;
import com.YOGIITSU.dto.ResponseDto.ReviewResponseDto;
import com.YOGIITSU.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = {"https://web.yogiitsu.app", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

	private final ReviewService reviewService;

	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_LIMIT = 6;

	@GetMapping
	public ResponseEntity<ReviewPageResponseDto> getAllReviews(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "6") int limit
	) {
		// 페이지와 limit 유효성 검사
		if (page < 1) {
			page = DEFAULT_PAGE;
		}
		if (limit < 1) {
			limit = DEFAULT_LIMIT;
		}

		ReviewPageResponseDto response = reviewService.getReviewsWithPagination(page, limit);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<ReviewResponseDto> createReview(
		@Valid @RequestBody ReviewCreateRequestDto request) {
		ReviewResponseDto createdReview = reviewService.createReview(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
		reviewService.deleteReview(id);
		return ResponseEntity.noContent().build();
	}
}
