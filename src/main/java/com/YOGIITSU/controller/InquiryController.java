package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.dto.RequestDto.InquiryRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryListResponseDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<Void> createInquiry(
        @RequestBody InquiryRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        // JWT 토큰에서 memberId 추출
        inquiryService.createInquiry(requestDto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 전체 문의 리스트 조회 API
     * - 로그인 한 모든 사용자가 확인 가능
     */
    @Operation(summary = "전체 문의 목록 조회")
    @GetMapping
    public ResponseEntity<List<InquiryListResponseDto>> getAllInquiries(
        HttpServletRequest request) {
        // 1. 요청에서 JWT 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);

        // 2. 토큰 없으면 401 에러
        if (token == null) {
            throw new MissingTokenException(); // custom 예외: "토큰 없음"
        }

        // 3. 토큰이 유효하지 않으면 401 에러
        if (!jwtTokenProvider.validateToken(token)) {
            throw new InvalidTokenException(); // custom 예외: "유효하지 않은 토큰"
        }

        // 토큰 유효하면 전체 문의 리스트 반환
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }

    /**
     * 나의 문의 조회 API
     * - 인증된 사용자만 본인의 문의를 확인 가능
     */
    @Operation(summary = "나의 문의 조회")
    @GetMapping("/{inquiryId}")
    public ResponseEntity<InquiryResponseDto> getInquiry(
        @PathVariable Long inquiryId,
        HttpServletRequest request) {

        // JWT 토큰에서 memberId 추출
        Long memberId = extractMemberIdFromToken(request);

        // 문의 단건 조회
        InquiryResponseDto response = inquiryService.getInquiry(inquiryId, memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 문의 수정 API
     * - 본인의 문의이며, 답변 대기 상태인 경우에만 수정 가능
     */
    @Operation(summary = "문의 수정")
    @PutMapping("/{inquiryId}")
    public ResponseEntity<InquiryResponseDto> updateInquiry(
        @PathVariable Long inquiryId,
        @RequestBody @Valid InquiryRequestDto requestDto,
        HttpServletRequest request) {

        // JWT 토큰에서 memberId 추출
        Long memberId = extractMemberIdFromToken(request);

        // 문의 수정
        InquiryResponseDto response = inquiryService.updateInquiry(inquiryId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 문의 삭제 API
     * - 본인의 문의이며, 답변 대기 상태인 경우에만 삭제 가능
     */
    @Operation(summary = "문의 삭제")
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Void> deleteInquiry(
        @PathVariable Long inquiryId,
        HttpServletRequest request) {

        // JWT 토큰에서 memberId 추출
        Long memberId = extractMemberIdFromToken(request);

        // 문의 삭제
        inquiryService.deleteInquiry(inquiryId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * JWT 토큰에서 memberId 추출
     */
    private Long extractMemberIdFromToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);

        if (token == null) {
            throw new MissingTokenException();
        }
        if (!jwtTokenProvider.validateToken(token)) {
            throw new InvalidTokenException();
        }

        String memberId = jwtTokenProvider.getAuthentication(token).getName();

        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return member.getId();
    }

}
