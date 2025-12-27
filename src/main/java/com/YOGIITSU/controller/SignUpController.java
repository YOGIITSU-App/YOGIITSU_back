package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.MemberSignUpRequestDto;
import com.YOGIITSU.service.SignUpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 회원가입과 관련된 요청을 처리하는 API
 */
@Tag(name = "회원가입 API", description = "이메일 인증 후 회원가입 정보를 등록합니다.")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class SignUpController {

	private final SignUpService signUpService;

	@Operation(
		summary = "회원가입",
		description = "이메일 인증을 마친 사용자의 회원가입 정보를 등록합니다."
	)
	@PostMapping("/signup")
	public ResponseEntity<Map<String, String>> signUp(
		@RequestBody @Valid MemberSignUpRequestDto memberSignUpRequestDto) {

		signUpService.register(memberSignUpRequestDto);

		return ResponseEntity.ok(Map.of("message", "회원가입이 성공적으로 완료되었습니다."));
	}
}
