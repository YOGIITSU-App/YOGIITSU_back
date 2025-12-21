package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.SearchSuggestionResponseDto;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.service.SearchSuggestionService;
import com.YOGIITSU.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = SearchSuggestionController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class,
		OAuth2ResourceServerAutoConfiguration.class
	}
)
class SearchSuggestionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SearchSuggestionService searchSuggestionService;

	@MockBean
	private JwtUtil jwtUtil;

	@DisplayName("자동완성_성공_업데이트타입별케이스")
	@ParameterizedTest(name = "memberId={0}")
	@CsvSource(value = {"member1", "NULL"}, nullValues = "NULL")
	void getSearchSuggestions_success(String memberId) throws Exception {
		// given
		String query = "대학";
		List<SearchSuggestionResponseDto> response = List.of(
			new SearchSuggestionResponseDto(1L, "대학본관", true, List.of("태그1")),
			new SearchSuggestionResponseDto(2L, "대학도서관", false, List.of("태그2"))
		);

		when(jwtUtil.extractMemberIdStringSafely(any())).thenReturn(memberId);
		when(searchSuggestionService.getSearchSuggestions(query, memberId)).thenReturn(response);

		// when & then
		mockMvc.perform(get("/search/suggestions")
				.param("query", query)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].keyword").value("대학본관"))
			.andExpect(jsonPath("$[0].bookmarked").value(true));

		verify(jwtUtil).extractMemberIdStringSafely(any());
		verify(searchSuggestionService).getSearchSuggestions(query, memberId);
	}

	@DisplayName("자동완성_성공_토큰없음")
	@Test
	void getSearchSuggestions_success_noToken() throws Exception {
		// given
		String query = "도서";
		when(jwtUtil.extractMemberIdStringSafely(any())).thenReturn(null);
		when(searchSuggestionService.getSearchSuggestions(query, null))
			.thenReturn(Collections.emptyList());

		// when & then
		mockMvc.perform(get("/search/suggestions")
				.param("query", query)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));

		verify(jwtUtil).extractMemberIdStringSafely(any());
		verify(searchSuggestionService).getSearchSuggestions(query, null);
	}

	@DisplayName("자동완성_성공_빈문자열쿼리시_빈리스트")
	@Test
	void getSearchSuggestions_success_blankQuery() throws Exception {
		// when & then
		mockMvc.perform(get("/search/suggestions")
				.param("query", " ")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));

		verify(searchSuggestionService, never()).getSearchSuggestions(any(), any());
	}

	@DisplayName("자동완성_성공_쿼리파라미터없음")
	@Test
	void getSearchSuggestions_success_missingQuery() throws Exception {
		// when & then
		mockMvc.perform(get("/search/suggestions")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));

		verify(searchSuggestionService, never()).getSearchSuggestions(any(), any());
	}

	@DisplayName("자동완성_실패_회원없음")
	@Test
	void getSearchSuggestions_fail_memberNotFound() throws Exception {
		// given
		String query = "대학";
		String memberId = "missing";
		when(jwtUtil.extractMemberIdStringSafely(any())).thenReturn(memberId);
		when(searchSuggestionService.getSearchSuggestions(query, memberId))
			.thenThrow(new MemberNotFoundException(memberId));

		// when & then
		mockMvc.perform(get("/search/suggestions")
				.param("query", query)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());

		verify(jwtUtil).extractMemberIdStringSafely(any());
		verify(searchSuggestionService).getSearchSuggestions(query, memberId);
	}
}

