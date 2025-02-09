package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.InvalidTokenException;
import com.YOGIITSU.config.handler.GlobalExceptionHandler.MissingTokenException;
import com.YOGIITSU.dto.MemberLoginRequestDto;
import com.YOGIITSU.dto.RequestDto.FindMemberIdRequestDto;
import com.YOGIITSU.dto.RequestDto.PasswordCheckRequestDto;
import com.YOGIITSU.dto.ResponseDto.FindMemberIdResponseDto;
import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;

	private static final String NO_EMAIL_FOUND = "가입 이력이 없는 이메일입니다.";
	private static final String EMAIL_MATCH_FOUND = "이메일 정보와 일치하는 아이디가 있습니다.";
	private static final String MEMBER_DELETION_SUCCESS = "님의 회원 탈퇴가 완료되었습니다.";

	@PostMapping("/login")
	public TokenResponseDto login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {
		return memberService.login(
			memberLoginRequestDto.getMemberId(),
			memberLoginRequestDto.getPassword()
		);
	}

	/**
	 * 아이디 찾기 API
	 *
	 * @param request 요청 데이터 (이메일)
	 * @return 아이디 찾기 결과
	 */
	@PostMapping("/find-id")
	public ResponseEntity<FindMemberIdResponseDto> findId(
		@RequestBody FindMemberIdRequestDto request) {
		String email = request.getEmail();
		String memberId = memberService.findIdByEmail(email);

		FindMemberIdResponseDto response = FindMemberIdResponseDto.builder()
			.status(memberId != null ? "success" : "error")
			.id(memberId)
			.message(memberId == null ? NO_EMAIL_FOUND : EMAIL_MATCH_FOUND)
			.build();

		return ResponseEntity.ok(response);
	}

	/**
	 * 회원 탈퇴 API
	 *
	 * @param request HTTP 요청 객체
	 */
	@DeleteMapping("/delete")
	public ResponseEntity<Map<String, String>> deleteMember(
		@RequestBody PasswordCheckRequestDto request, HttpServletRequest httpRequest) {
		// 1. 요청에서 JWT 토큰 추출
		String accessToken = jwtTokenProvider.resolveToken(httpRequest);

		// 2. 토큰이 없으면 예외 발생
		if (accessToken == null) {
			throw new MissingTokenException();
		}

		// 3. 토큰이 유효한지 확인, 유효하지 않으면 예외 발생
		if (!jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 4. 유효한 토큰이라면 사용자 ID 추출
		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();

		// 5. 회원 탈퇴 처리
		memberService.deleteMember(memberId, request.getPassword());

		// 6. 탈퇴 성공 메시지 반환
		Map<String, String> response = new HashMap<>();
		response.put("message", memberId + MEMBER_DELETION_SUCCESS);
		return ResponseEntity.ok(response);
	}
}
