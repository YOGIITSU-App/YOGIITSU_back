package com.YOGIITSU.controller;

import com.YOGIITSU.exception.auth.InvalidTokenException;
import com.YOGIITSU.exception.auth.MissingTokenException;
import com.YOGIITSU.exception.user.EmailNotRegisteredException;
import com.YOGIITSU.dto.RequestDto.ChangePasswordRequestDto;
import com.YOGIITSU.dto.RequestDto.MemberLoginRequestDto;
import com.YOGIITSU.dto.RequestDto.FindMemberIdRequestDto;
import com.YOGIITSU.dto.RequestDto.PasswordResetRequestDto;
import com.YOGIITSU.dto.ResponseDto.FindMemberIdResponseDto;
import com.YOGIITSU.dto.ResponseDto.MemberCountResponseDto;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "회원 관련 API", description = "로그인, 아이디 찾기, 비밀번호 재설정, 탈퇴 등 회원 기능 제공")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

	private final MemberService memberService;
	private final JwtTokenProvider jwtTokenProvider;

	private static final String EMAIL_MATCH_FOUND = "이메일 정보와 일치하는 아이디가 있습니다.";
	private static final String MEMBER_DELETION_SUCCESS = "님의 회원 탈퇴가 완료되었습니다.";

	/**
	 * 로그인 API
	 *
	 * @param memberLoginRequestDto 요청 데이터 (아이디, 비밀번호)
	 * @return TokenInfo JWT 토큰 정보
	 */
	@Operation(summary = "로그인", description = "아이디와 비밀번호를 이용해 로그인합니다.")
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(
		@RequestBody MemberLoginRequestDto memberLoginRequestDto) {
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
	@Operation(summary = "아이디 찾기", description = "이메일을 기반으로 등록된 아이디를 조회합니다.")
	@PostMapping("/find-id")
	public ResponseEntity<FindMemberIdResponseDto> findId(
		@RequestBody @Valid FindMemberIdRequestDto request) {
		String email = request.getEmail();
		String memberId = memberService.findIdByEmail(email);

		if (memberId == null) {
			throw new EmailNotRegisteredException();
		}

		FindMemberIdResponseDto response = FindMemberIdResponseDto.builder()
			.message(EMAIL_MATCH_FOUND)
			.id(memberId)
			.build();

		return ResponseEntity.ok(response);
	}

	/**
	 * 회원 탈퇴 API
	 * @param httpRequest HTTP 요청 객체 (JWT 토큰 포함)
	 */
	@Operation(summary = "회원 탈퇴", description = "JWT 토큰 기반으로 현재 로그인된 회원을 탈퇴 처리합니다.")
	@DeleteMapping("/delete")
	public ResponseEntity<Map<String, String>> deleteMember(HttpServletRequest httpRequest) {
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
		memberService.deleteMember(memberId);

		// 6. 탈퇴 성공 메시지 반환
		Map<String, String> response = new HashMap<>();
		response.put("message", memberId + MEMBER_DELETION_SUCCESS);
		return ResponseEntity.ok(response);
	}

	/**
	 * 비밀번호 변경 API
	 *
	 * @param requestDto  새로운 비밀번호와 확인 비밀번호
	 * @param httpRequest HTTP 요청 객체
	 * @return 비밀번호 변경 결과
	 */
	@Operation(summary = "비밀번호 변경", description = "로그인된 상태에서 새 비밀번호로 변경합니다.")
	@PatchMapping("/change-password")
	public ResponseEntity<Map<String, String>> changePassword(
		@RequestBody ChangePasswordRequestDto requestDto, HttpServletRequest httpRequest) {

		// 1. JWT 토큰 추출 및 유효성 검사
		String accessToken = jwtTokenProvider.resolveToken(httpRequest);
		if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
			throw new InvalidTokenException();
		}

		// 2. 현재 인증된 사용자 가져오기
		Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
		String memberId = authentication.getName();

		// 3. 비밀번호 변경 처리
		memberService.changePassword(memberId, requestDto.getNewPassword(),
			requestDto.getConfirmPassword());

		// 4. 성공 메시지 반환
		return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
	}

	/**
	 * 비밀번호 찾기 (재설정) 로그인되지 않은 상태 -> 이메일 인증 완료 여부를 기반으로 비밀번호 재설정 허용
	 *
	 * @param requestDto 요청 데이터 (이메일, 새 비밀번호, 확인 비밀번호)
	 * @return 비밀번호 재설정 결과
	 */
	@Operation(summary = "비밀번호 찾기 (재설정)", description = "비밀번호를 찾기 위해 이메일 인증을 완료한 사용자가 비밀번호를 재설정합니다.")
	@PostMapping("/find-password")
	public ResponseEntity<Map<String, String>> resetPassword(
		@RequestBody PasswordResetRequestDto requestDto) {

		memberService.resetPasswordAfterEmailVerification(requestDto);

		return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
	}

	/**
	 * 전체 회원 수 조회 API (web)
	 *
	 * @return 전체 회원 수
	 */
	@Operation(summary = "전체 회원 수 조회", description = "현재 가입된 전체 회원 수를 조회합니다.")
	@CrossOrigin(origins = {"https://web.yogiitsu.app", "http://localhost:3000"})
	@GetMapping("/count")
	public ResponseEntity<MemberCountResponseDto> getMemberCount() {
		Long memberCount = memberService.getMemberCount();
		return ResponseEntity.ok(new MemberCountResponseDto(memberCount));
	}
}
