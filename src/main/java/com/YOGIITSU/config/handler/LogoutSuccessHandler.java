package com.YOGIITSU.config.handler;

import com.YOGIITSU.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.io.IOException;

public class LogoutSuccessHandler implements
	org.springframework.security.web.authentication.logout.LogoutSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	private static final String CHARSET = "UTF-8";
	private static final String LOGOUT_SUCCESS_MESSAGE = "님의 로그아웃이 완료되었습니다.";
	private static final String LOGOUT_FAILURE_MESSAGE = "로그아웃 실패: 존재하지 않는 토큰입니다.";
	private static final String TOKEN_NOT_FOUND_MESSAGE = "로그아웃 실패: 토큰을 입력해 주세요.";

	/**
	 * 생성자: JwtTokenProvider 주입
	 *
	 * @param jwtTokenProvider JWT 토큰을 처리하는 provider 클래스
	 */
	@Autowired
	public LogoutSuccessHandler(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/**
	 * 로그아웃 성공 시 호출되는 메서드
	 *
	 * @param request        HTTP 요청 객체
	 * @param response       HTTP 응답 객체
	 * @param authentication 인증된 사용자 정보
	 * @throws IOException 응답 작성 시 발생할 수 있는 예외
	 */
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		// 요청에서 JWT 토큰 추출
		String accessToken = jwtTokenProvider.resolveToken(request);

		// 응답 콘텐츠 타입과 문자 인코딩 설정
		response.setContentType(CONTENT_TYPE);
		response.setCharacterEncoding(CHARSET);

		// 토큰이 있을 경우 유효성 검사
		if (accessToken != null) {
			if (jwtTokenProvider.validateToken(accessToken)) {
				// 유효한 토큰이라면 인증 정보 추출
				Authentication auth = jwtTokenProvider.getAuthentication(accessToken);
				String memberId = auth.getName(); // 인증된 회원의 ID

				// 로그아웃 성공 메시지 반환
				writeResponse(response, HttpServletResponse.SC_OK,
					memberId + LOGOUT_SUCCESS_MESSAGE);
			} else {
				// 유효하지 않은 토큰일 경우 실패 메시지 반환
				writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, LOGOUT_FAILURE_MESSAGE);
			}
		} else {
			// 토큰이 없는 경우 실패 메시지 반환
			writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, TOKEN_NOT_FOUND_MESSAGE);
		}
	}

	/**
	 * 응답 작성 메서드
	 *
	 * @param response 응답 객체
	 * @param status   HTTP 상태 코드
	 * @param message  응답 메시지
	 * @throws IOException 응답 작성 시 발생할 수 있는 예외
	 */
	private void writeResponse(HttpServletResponse response, int status, String message)
		throws IOException {
		response.setStatus(status);
		response.getWriter().write("{\"message\": \"" + message + "\"}");
	}
}