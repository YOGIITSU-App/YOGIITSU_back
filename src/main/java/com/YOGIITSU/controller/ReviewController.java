package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.ReviewCreateRequestDto;
import com.YOGIITSU.dto.ResponseDto.ReviewResponseDto;
import com.YOGIITSU.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = {"https://web.yogiitsu.app", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

	private final ReviewService reviewService;

	@GetMapping
	public ResponseEntity<List<ReviewResponseDto>> getAllReviews() {
		List<ReviewResponseDto> reviews = reviewService.getAllReviews();
		return ResponseEntity.ok(reviews);
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