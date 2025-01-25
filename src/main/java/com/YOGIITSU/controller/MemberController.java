package com.YOGIITSU.controller;

import com.YOGIITSU.dto.MemberLoginRequestDto;
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
}