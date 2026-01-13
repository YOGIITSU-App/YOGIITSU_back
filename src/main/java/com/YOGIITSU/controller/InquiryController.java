package com.YOGIITSU.controller;

import com.YOGIITSU.exception.auth.InvalidTokenException;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.dto.RequestDto.InquiryRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryListResponseDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * InquiryController
 * - 문의 관련 API 제공
 * - 등록, 전체 조회, 단건 조회, 수정, 삭제 기능 포함
 */
@Slf4j
@Tag(name = "문의 API", description = "문의 등록, 조회, 수정, 삭제 기능 제공")
@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiryController {

	private final InquiryService inquiryService;
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;

	/**
	 * 문의 등록 API
	 * - 인증된 사용자가 문의 등록
	 */
	@Operation(summary = "문의 등록")
	@PostMapping
	public ResponseEntity<String> createInquiry(
		@RequestBody @Valid InquiryRequestDto requestDto,
		HttpServletRequest request) {

		Long memberId = extractMemberIdFromToken(request); // 로그인 필수
		inquiryService.createInquiry(requestDto, memberId);

		return ResponseEntity.status(HttpStatus.CREATED).body("등록이 완료되었습니다.");
	}

	/**
	 * 전체 문의 리스트 조회 API
	 * - 비회원도 가능 (memberId=null일 경우 isMine=false로 처리)
	 */
	@Operation(summary = "전체 문의 목록 조회")
	@GetMapping
	public ResponseEntity<List<InquiryListResponseDto>> getAllInquiries(
		HttpServletRequest request) {

		Long memberId = extractMemberIdOrNull(request); // 토큰 없으면 Null
		return ResponseEntity.ok(inquiryService.getAllInquiries(memberId));
	}

	/**
	 * 문의 상세 조회 API
	 * - 비회원도 가능 (본인 여부는 isMine으로 표시)
	 */
	@Operation(summary = "문의 상세 조회")
	@GetMapping("/{inquiryId}")
	public ResponseEntity<InquiryResponseDto> getInquiry(
		@PathVariable Long inquiryId,
		HttpServletRequest request) {

		Long memberId = extractMemberIdOrNull(request); // 토큰 없으면 Null
		return ResponseEntity.ok(inquiryService.getInquiry(inquiryId, memberId));
	}

	/**
	 * 문의 수정 API
	 * - 로그인 필요 + 본인 문의만 가능 - 답변 대기 상태인 경우에만 수정 가능
	 */
	@Operation(summary = "문의 수정")
	@PutMapping("/{inquiryId}")
	public ResponseEntity<InquiryResponseDto> updateInquiry(
		@PathVariable Long inquiryId,
		@RequestBody InquiryRequestDto requestDto,
		HttpServletRequest request) {

		Long memberId = extractMemberIdFromToken(request);
		return ResponseEntity.ok(inquiryService.updateInquiry(inquiryId, memberId, requestDto));
	}

	/**
	 * 문의 삭제 API
	 * - 로그인 필요 + 본인 문의만 가능
	 */
	@Operation(summary = "문의 삭제")
	@DeleteMapping("/{inquiryId}")
	public ResponseEntity<Void> deleteInquiry(
		@PathVariable Long inquiryId,
		HttpServletRequest request) {

		Long memberId = extractMemberIdFromToken(request);
		inquiryService.deleteInquiry(inquiryId, memberId);

		return ResponseEntity.noContent().build();
	}

	/**
	 * JWT 토큰에서 memberId 추출 (로그인 필수)
	 * - 토큰 없거나 유효하지 않으면 예외 발생
	 */
	private Long extractMemberIdFromToken(HttpServletRequest request) {
		String token = jwtTokenProvider.resolveToken(request);

		if (token == null) {
			throw new InvalidTokenException("토큰이 존재하지 않습니다.");
		}
		if (!jwtTokenProvider.validateToken(token)) {
			throw new InvalidTokenException("유효하지 않은 토큰입니다.");
		}

		String memberId = jwtTokenProvider.getAuthentication(token).getName();
		return memberRepository.findByMemberId(memberId)
			.orElseThrow(MemberNotFoundException::new)
			.getId();
	}

	/**
	 * JWT 토큰에서 memberId 추출 (비회원 허용)
	 * - 토큰 없거나 유효하지 않으면 null 반환
	 */
	private Long extractMemberIdOrNull(HttpServletRequest request) {
		String token = jwtTokenProvider.resolveToken(request);

		// 토큰이 없으면 비회원으로 간주
		if (token == null) {
			return null;
		}

		// 토큰이 유효하지 않으면 비회원으로 간주
		if (!jwtTokenProvider.validateToken(token)) {
			return null;
		}

		String memberId = jwtTokenProvider.getAuthentication(token).getName();
		return memberRepository.findByMemberId(memberId)
			.map(member -> member.getId())
			.orElse(null);
	}
}
