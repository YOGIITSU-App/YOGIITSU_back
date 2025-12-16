package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.SearchKeywordRequestDto;
import com.YOGIITSU.dto.ResponseDto.RecentSearchResponseDto;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.RecentSearch;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.service.RecentSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
	controllers = RecentSearchController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class,
		OAuth2ResourceServerAutoConfiguration.class
	}
)
class RecentSearchControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RecentSearchService recentSearchService;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@DisplayName("검색어저장_성공")
	@Test
	@WithMockUser
	void saveSearchKeyword_success() throws Exception {
		// given
		String memberId = "testMember";
		String keyword = "테스트건물";
		String accessToken = "validToken";
		SearchKeywordRequestDto request = new SearchKeywordRequestDto(keyword);

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doNothing().when(recentSearchService).saveSearchKeyword(memberId, keyword);

		// when & then
		mockMvc.perform(post("/search/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("검색어가 저장되었습니다."));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(recentSearchService).saveSearchKeyword(memberId, keyword);
	}

	@DisplayName("검색어저장_실패_토큰없음")
	@Test
	void saveSearchKeyword_fail_missingToken() throws Exception {
		// given
		String keyword = "테스트건물";
		SearchKeywordRequestDto request = new SearchKeywordRequestDto(keyword);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(post("/search/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(recentSearchService, never()).saveSearchKeyword(any(), any());
	}

	@DisplayName("검색어저장_실패_유효하지않은토큰")
	@Test
	void saveSearchKeyword_fail_invalidToken() throws Exception {
		// given
		String keyword = "테스트건물";
		String accessToken = "invalidToken";
		SearchKeywordRequestDto request = new SearchKeywordRequestDto(keyword);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(post("/search/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService, never()).saveSearchKeyword(any(), any());
	}

	@DisplayName("검색어저장_실패_회원없음")
	@Test
	@WithMockUser
	void saveSearchKeyword_fail_memberNotFound() throws Exception {
		// given
		String memberId = "nonExistentMember";
		String keyword = "테스트건물";
		String accessToken = "validToken";
		SearchKeywordRequestDto request = new SearchKeywordRequestDto(keyword);

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doThrow(new MemberNotFoundException(memberId)).when(recentSearchService)
			.saveSearchKeyword(memberId, keyword);

		// when & then
		mockMvc.perform(post("/search/save")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService).saveSearchKeyword(memberId, keyword);
	}

	@DisplayName("최근검색어조회_성공")
	@Test
	@WithMockUser
	void getRecentSearches_success() throws Exception {
		// given
		String memberId = "testMember";
		String accessToken = "validToken";
		Member member = createDummyMember();
		Building building = createDummyBuilding();
		RecentSearch recentSearch1 = createDummyRecentSearch(member, "검색어1", building);
		RecentSearch recentSearch2 = createDummyRecentSearch(member, "검색어2", null);

		List<RecentSearchResponseDto> responseDtos = List.of(
			new RecentSearchResponseDto(recentSearch1),
			new RecentSearchResponseDto(recentSearch2)
		);

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		when(recentSearchService.getRecentSearches(memberId)).thenReturn(responseDtos);

		// when & then
		mockMvc.perform(get("/search/recent")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].keyword").value("검색어1"))
			.andExpect(jsonPath("$[0].buildingId").value(1L))
			.andExpect(jsonPath("$[1].keyword").value("검색어2"))
			.andExpect(jsonPath("$[1].buildingId").isEmpty());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(recentSearchService).getRecentSearches(memberId);
	}

	@DisplayName("최근검색어조회_성공_빈목록")
	@Test
	@WithMockUser
	void getRecentSearches_success_emptyList() throws Exception {
		// given
		String memberId = "testMember";
		String accessToken = "validToken";
		List<RecentSearchResponseDto> responseDtos = List.of();

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		when(recentSearchService.getRecentSearches(memberId)).thenReturn(responseDtos);

		// when & then
		mockMvc.perform(get("/search/recent")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(0)));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(recentSearchService).getRecentSearches(memberId);
	}

	@DisplayName("최근검색어조회_실패_토큰없음")
	@Test
	void getRecentSearches_fail_missingToken() throws Exception {
		// given
		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(get("/search/recent")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(recentSearchService, never()).getRecentSearches(any());
	}

	@DisplayName("최근검색어조회_실패_유효하지않은토큰")
	@Test
	void getRecentSearches_fail_invalidToken() throws Exception {
		// given
		String accessToken = "invalidToken";

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(get("/search/recent")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService, never()).getRecentSearches(any());
	}

	@DisplayName("최근검색어조회_실패_회원없음")
	@Test
	@WithMockUser
	void getRecentSearches_fail_memberNotFound() throws Exception {
		// given
		String memberId = "nonExistentMember";
		String accessToken = "validToken";

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		when(recentSearchService.getRecentSearches(memberId))
			.thenThrow(new MemberNotFoundException(memberId));

		// when & then
		mockMvc.perform(get("/search/recent")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService).getRecentSearches(memberId);
	}

	@DisplayName("검색어단건삭제_성공")
	@Test
	@WithMockUser
	void deleteByBuildingId_success() throws Exception {
		// given
		Long buildingId = 1L;
		String memberId = "testMember";
		String accessToken = "validToken";

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doNothing().when(recentSearchService).deleteSearchKeywordByBuildingId(memberId, buildingId);

		// when & then
		mockMvc.perform(delete("/search/delete/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("해당 건물의 검색어가 삭제되었습니다."));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(recentSearchService).deleteSearchKeywordByBuildingId(memberId, buildingId);
	}

	@DisplayName("검색어단건삭제_실패_토큰없음")
	@Test
	void deleteByBuildingId_fail_missingToken() throws Exception {
		// given
		Long buildingId = 1L;

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(delete("/search/delete/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(recentSearchService, never()).deleteSearchKeywordByBuildingId(any(), any());
	}

	@DisplayName("검색어단건삭제_실패_유효하지않은토큰")
	@Test
	void deleteByBuildingId_fail_invalidToken() throws Exception {
		// given
		Long buildingId = 1L;
		String accessToken = "invalidToken";

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(delete("/search/delete/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService, never()).deleteSearchKeywordByBuildingId(any(), any());
	}

	@DisplayName("검색어단건삭제_실패_회원없음")
	@Test
	@WithMockUser
	void deleteByBuildingId_fail_memberNotFound() throws Exception {
		// given
		Long buildingId = 1L;
		String memberId = "nonExistentMember";
		String accessToken = "validToken";

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doThrow(new MemberNotFoundException(memberId)).when(recentSearchService)
			.deleteSearchKeywordByBuildingId(memberId, buildingId);

		// when & then
		mockMvc.perform(delete("/search/delete/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService).deleteSearchKeywordByBuildingId(memberId, buildingId);
	}

	@DisplayName("검색어단건삭제_실패_검색어없음")
	@Test
	@WithMockUser
	void deleteByBuildingId_fail_searchNotFound() throws Exception {
		// given
		Long buildingId = 999L;
		String memberId = "testMember";
		String accessToken = "validToken";

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doThrow(new InvalidArgumentException("해당 건물과 연결된 검색어가 없습니다."))
			.when(recentSearchService).deleteSearchKeywordByBuildingId(memberId, buildingId);

		// when & then
		mockMvc.perform(delete("/search/delete/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService).deleteSearchKeywordByBuildingId(memberId, buildingId);
	}

	@DisplayName("검색어전체삭제_성공")
	@Test
	@WithMockUser
	void deleteAllKeywords_success() throws Exception {
		// given
		String memberId = "testMember";
		String accessToken = "validToken";

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doNothing().when(recentSearchService).deleteAllSearchKeywords(memberId);

		// when & then
		mockMvc.perform(delete("/search/deleteAll")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("전체 검색어가 삭제되었습니다."));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(recentSearchService).deleteAllSearchKeywords(memberId);
	}

	@DisplayName("검색어전체삭제_실패_토큰없음")
	@Test
	void deleteAllKeywords_fail_missingToken() throws Exception {
		// given
		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(delete("/search/deleteAll")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(recentSearchService, never()).deleteAllSearchKeywords(any());
	}

	@DisplayName("검색어전체삭제_실패_유효하지않은토큰")
	@Test
	void deleteAllKeywords_fail_invalidToken() throws Exception {
		// given
		String accessToken = "invalidToken";

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(delete("/search/deleteAll")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService, never()).deleteAllSearchKeywords(any());
	}

	@DisplayName("검색어전체삭제_실패_회원없음")
	@Test
	@WithMockUser
	void deleteAllKeywords_fail_memberNotFound() throws Exception {
		// given
		String memberId = "nonExistentMember";
		String accessToken = "validToken";

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		doThrow(new MemberNotFoundException(memberId)).when(recentSearchService)
			.deleteAllSearchKeywords(memberId);

		// when & then
		mockMvc.perform(delete("/search/deleteAll")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(recentSearchService).deleteAllSearchKeywords(memberId);
	}

	private Member createDummyMember() {
		return Member.builder()
			.id(1L)
			.memberId("testMember")
			.password("password")
			.email("test@example.com")
			.userName("테스트사용자")
			.role("USER")
			.joinAt(LocalDateTime.now())
			.provider("local")
			.build();
	}

	private Building createDummyBuilding() {
		return Building.builder()
			.id(1L)
			.name("테스트건물")
			.latitude(37.123456)
			.longitude(127.123456)
			.imageUrl("test.jpg")
			.build();
	}

	private RecentSearch createDummyRecentSearch(Member member, String keyword, Building building) {
		return RecentSearch.builder()
			.id(1L)
			.member(member)
			.keyword(keyword)
			.searchedAt(LocalDateTime.now())
			.building(building)
			.build();
	}
}

