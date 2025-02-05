package com.YOGIITSU.controller;

import com.YOGIITSU.dto.MemberLoginRequestDto;
import com.YOGIITSU.dto.RequestDto.FindMemberIdRequestDto;
import com.YOGIITSU.dto.ResponseDto.FindMemberIdResponseDto;
import com.YOGIITSU.dto.TokenInfo;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;

	private static final String INVALID_TOKEN_MESSAGE = "회원 탈퇴 실패: 존재하지 않는 토큰입니다.";
	private static final String MISSING_TOKEN_MESSAGE = "회원 탈퇴 실패: 토큰을 입력해 주세요.";

	@PostMapping("/login")
	public TokenInfo login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {
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
			.message(memberId == null ? "가입 이력이 없는 이메일입니다." : "이메일 정보와 일치하는 아이디가 있습니다.")
			.build();

		return ResponseEntity.ok(response);
	}

	/**
	 * 회원 탈퇴 API
	 *
	 * @param request HTTP 요청 객체
	 */
	@DeleteMapping("/delete")
	public ResponseEntity<String> deleteMember(HttpServletRequest request) {
		// 1. 요청에서 JWT 토큰 추출
		String accessToken = jwtTokenProvider.resolveToken(request);

		// 2. 토큰이 없으면 에러 응답
		if (accessToken == null) {
			return ResponseEntity.badRequest().body(MISSING_TOKEN_MESSAGE);
		}

		// 3. 토큰이 유효한지 확인
		if (!jwtTokenProvider.validateToken(accessToken)) {
			return ResponseEntity.badRequest().body(INVALID_TOKEN_MESSAGE);
		}

		// 4. 유효한 토큰이라면 사용자 ID 추출
		String memberId = jwtTokenProvider.getAuthentication(accessToken).getName();

		// 5. 회원 탈퇴 처리
		memberService.deleteMember(memberId);

		// 6. 탈퇴 성공 메시지 반환
		return ResponseEntity.ok(memberId + "님의 회원 탈퇴가 완료되었습니다.");
	}
}
