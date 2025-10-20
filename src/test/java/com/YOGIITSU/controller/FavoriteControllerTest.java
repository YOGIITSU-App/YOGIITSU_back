package com.YOGIITSU.controller;

import com.YOGIITSU.entity.Favorite;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.exception.resource.FavoriteAlreadyExistsException;
import com.YOGIITSU.exception.resource.FavoriteNotFoundException;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.service.FavoriteService;
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
	controllers = FavoriteController.class,
	excludeAutoConfiguration = {
		SecurityAutoConfiguration.class,
		UserDetailsServiceAutoConfiguration.class,
		OAuth2ClientAutoConfiguration.class,
		OAuth2ResourceServerAutoConfiguration.class
	}
)
class FavoriteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FavoriteService favoriteService;

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	@DisplayName("즐겨찾기추가_성공")
	@Test
	@WithMockUser
	void addFavorite_success() throws Exception {
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
		doNothing().when(favoriteService).addFavorite(memberId, buildingId);

		// when & then
		mockMvc.perform(post("/favorites/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("즐겨찾기에 추가되었습니다."));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(favoriteService).addFavorite(memberId, buildingId);
	}

	@DisplayName("즐겨찾기추가_실패_토큰없음")
	@Test
	void addFavorite_fail_missingToken() throws Exception {
		// given
		Long buildingId = 1L;

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(post("/favorites/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(favoriteService, never()).addFavorite(any(), any());
	}

	@DisplayName("즐겨찾기추가_실패_유효하지않은토큰")
	@Test
	void addFavorite_fail_invalidToken() throws Exception {
		// given
		Long buildingId = 1L;
		String accessToken = "invalidToken";

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(post("/favorites/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(favoriteService, never()).addFavorite(any(), any());
	}

	@DisplayName("즐겨찾기추가_실패_이미존재")
	@Test
	@WithMockUser
	void addFavorite_fail_alreadyExists() throws Exception {
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
		doThrow(new FavoriteAlreadyExistsException()).when(favoriteService)
			.addFavorite(memberId, buildingId);

		// when & then
		mockMvc.perform(post("/favorites/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isConflict());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(favoriteService).addFavorite(memberId, buildingId);
	}

	@DisplayName("즐겨찾기삭제_성공")
	@Test
	@WithMockUser
	void removeFavorite_success() throws Exception {
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
		doNothing().when(favoriteService).removeFavorite(memberId, buildingId);

		// when & then
		mockMvc.perform(delete("/favorites/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("즐겨찾기에서 삭제되었습니다."));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(favoriteService).removeFavorite(memberId, buildingId);
	}

	@DisplayName("즐겨찾기삭제_실패_토큰없음")
	@Test
	void removeFavorite_fail_missingToken() throws Exception {
		// given
		Long buildingId = 1L;

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(delete("/favorites/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(favoriteService, never()).removeFavorite(any(), any());
	}

	@DisplayName("즐겨찾기삭제_실패_유효하지않은토큰")
	@Test
	void removeFavorite_fail_invalidToken() throws Exception {
		// given
		Long buildingId = 1L;
		String accessToken = "invalidToken";

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(delete("/favorites/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(favoriteService, never()).removeFavorite(any(), any());
	}

	@DisplayName("즐겨찾기삭제_실패_즐겨찾기없음")
	@Test
	@WithMockUser
	void removeFavorite_fail_favoriteNotFound() throws Exception {
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
		doThrow(new FavoriteNotFoundException()).when(favoriteService)
			.removeFavorite(memberId, buildingId);

		// when & then
		mockMvc.perform(delete("/favorites/{buildingId}", buildingId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(favoriteService).removeFavorite(memberId, buildingId);
	}

	@DisplayName("즐겨찾기목록조회_성공")
	@Test
	@WithMockUser
	void getFavorites_success() throws Exception {
		// given
		String memberId = "testMember";
		String accessToken = "validToken";
		Member member = createDummyMember();
		Building building1 = createDummyBuilding();
		Building building2 = Building.builder()
			.id(2L)
			.name("테스트건물2")
			.latitude(37.123456)
			.longitude(127.123456)
			.imageUrl("test2.jpg")
			.build();

		List<Favorite> favorites = List.of(
			createDummyFavorite(member, building1),
			createDummyFavorite(member, building2)
		);

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		when(favoriteService.getFavorites(memberId)).thenReturn(favorites);

		// when & then
		mockMvc.perform(get("/favorites")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberId").value(memberId))
			.andExpect(jsonPath("$.buildings", hasSize(2)))
			.andExpect(jsonPath("$.buildings[0].buildingId").value(1L))
			.andExpect(jsonPath("$.buildings[0].buildingName").value("테스트건물"))
			.andExpect(jsonPath("$.buildings[1].buildingId").value(2L))
			.andExpect(jsonPath("$.buildings[1].buildingName").value("테스트건물2"));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(favoriteService).getFavorites(memberId);
	}

	@DisplayName("즐겨찾기목록조회_성공_빈목록")
	@Test
	@WithMockUser
	void getFavorites_success_emptyList() throws Exception {
		// given
		String memberId = "testMember";
		String accessToken = "validToken";
		List<Favorite> favorites = List.of();

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		when(favoriteService.getFavorites(memberId)).thenReturn(favorites);

		// when & then
		mockMvc.perform(get("/favorites")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.memberId").value(memberId))
			.andExpect(jsonPath("$.buildings", hasSize(0)));

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(jwtTokenProvider).getAuthentication(accessToken);
		verify(favoriteService).getFavorites(memberId);
	}

	@DisplayName("즐겨찾기목록조회_실패_토큰없음")
	@Test
	void getFavorites_fail_missingToken() throws Exception {
		// given
		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

		// when & then
		mockMvc.perform(get("/favorites")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider, never()).validateToken(any());
		verify(favoriteService, never()).getFavorites(any());
	}

	@DisplayName("즐겨찾기목록조회_실패_유효하지않은토큰")
	@Test
	void getFavorites_fail_invalidToken() throws Exception {
		// given
		String accessToken = "invalidToken";

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);

		// when & then
		mockMvc.perform(get("/favorites")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(favoriteService, never()).getFavorites(any());
	}

	@DisplayName("즐겨찾기목록조회_실패_회원없음")
	@Test
	@WithMockUser
	void getFavorites_fail_memberNotFound() throws Exception {
		// given
		String memberId = "nonExistentMember";
		String accessToken = "validToken";

		// Authentication 객체 모킹
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn(memberId);

		when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(accessToken);
		when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
		when(jwtTokenProvider.getAuthentication(accessToken)).thenReturn(authentication);
		when(favoriteService.getFavorites(memberId))
			.thenThrow(new MemberNotFoundException(memberId));

		// when & then
		mockMvc.perform(get("/favorites")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());

		verify(jwtTokenProvider).resolveToken(any(HttpServletRequest.class));
		verify(jwtTokenProvider).validateToken(accessToken);
		verify(favoriteService).getFavorites(memberId);
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

	private Favorite createDummyFavorite(Member member, Building building) {
		return Favorite.builder()
			.id(1L)
			.member(member)
			.building(building)
			.build();
	}
}