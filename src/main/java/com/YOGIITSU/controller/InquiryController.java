package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.dto.RequestDto.InquiryRequestDto;
import com.YOGIITSU.dto.RequestDto.PasswordCheckRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryListResponseDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.service.InquiryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    /**
     * Create: 문의 등록
     */
    @PostMapping
    public ResponseEntity<InquiryResponseDto> createInquiry(
        @RequestBody InquiryRequestDto requestDto,
        HttpServletRequest request) {

        // JWT 토큰에서 memberId 추출
        Long memberId = extractMemberIdFromToken(request);

        // 문의 등록
        InquiryResponseDto response = inquiryService.createInquiry(requestDto, memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * Read: 모든 문의 리스트 조회 (비밀번호 없이 제목, 상태, 작성자만 반환)
     */
    @GetMapping
    public ResponseEntity<List<InquiryListResponseDto>> getAllInquiries() {
        return ResponseEntity.ok(inquiryService.getAllInquiries());
    }

    /**
     * Read: 나의 문의 확인 (비밀번호 검증 후 내용 조회)
     */
    @PostMapping("/{inquiryId}/check")
    public ResponseEntity<InquiryResponseDto> getInquiry(
        @PathVariable Long inquiryId,
        @RequestBody PasswordCheckRequestDto passwordDto,
        HttpServletRequest request) {

        // JWT 토큰에서 memberId 추출
        Long memberId = extractMemberIdFromToken(request);

        // 문의 단건 조회
        InquiryResponseDto response = inquiryService.getInquiry(inquiryId, memberId,
            passwordDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Update: 문의 수정
     */
    @PutMapping("/{inquiryId}")
    public ResponseEntity<InquiryResponseDto> updateInquiry(
        @PathVariable Long inquiryId,
        @RequestBody InquiryRequestDto requestDto,
        HttpServletRequest request) {

        // JWT 토큰에서 memberId 추출
        Long memberId = extractMemberIdFromToken(request);

        // 문의 수정
        InquiryResponseDto response = inquiryService.updateInquiry(inquiryId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete: 문의 삭제
     */
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
     * JWT 토큰에서 memberId 추출하는 메서드
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
