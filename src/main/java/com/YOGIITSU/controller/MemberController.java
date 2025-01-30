package com.YOGIITSU.controller;

import com.YOGIITSU.dto.MemberLoginRequestDto;
import com.YOGIITSU.dto.RequestDto.FindMemberIdRequestDto;
import com.YOGIITSU.dto.ResponseDto.FindMemberIdResponseDto;
import com.YOGIITSU.dto.TokenInfo;
import com.YOGIITSU.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

	private final MemberService memberService;

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
}