package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.RecentSearchResponseDto;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.entity.BuildingAlias;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.RecentSearch;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.exception.validation.InvalidArgumentException;
import com.YOGIITSU.repository.BuildingAliasRepository;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.repository.RecentSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecentSearchServiceTest {

	@Mock
	private RecentSearchRepository recentSearchRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private BuildingAliasRepository buildingAliasRepository;

	@InjectMocks
	private RecentSearchService recentSearchService;

	@DisplayName("검색어저장_성공_건물매칭됨")
	@Test
	void saveSearchKeyword_success_withBuilding() {
		// given
		String memberId = "testMember";
		String keyword = "테스트건물";
		Member member = createDummyMember();
		Building building = createDummyBuilding();
		BuildingAlias buildingAlias = createDummyBuildingAlias(building, keyword);
		RecentSearch recentSearch = createDummyRecentSearch(member, keyword, building);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		doNothing().when(recentSearchRepository).deleteByMemberAndKeyword(member, keyword);
		when(recentSearchRepository.findByMemberOrderBySearchedAtDesc(member)).thenReturn(
			new ArrayList<>());
		when(buildingAliasRepository.findFirstByAliasContainingOrderByIdAsc(keyword))
			.thenReturn(Optional.of(buildingAlias));
		when(recentSearchRepository.save(any(RecentSearch.class))).thenReturn(recentSearch);

		// when
		assertDoesNotThrow(() -> recentSearchService.saveSearchKeyword(memberId, keyword));

		// then
		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository).deleteByMemberAndKeyword(member, keyword);
		verify(recentSearchRepository).flush();
		verify(recentSearchRepository).findByMemberOrderBySearchedAtDesc(member);
		verify(buildingAliasRepository).findFirstByAliasContainingOrderByIdAsc(keyword);
		verify(recentSearchRepository).save(any(RecentSearch.class));
	}

	@DisplayName("검색어저장_성공_건물매칭안됨")
	@Test
	void saveSearchKeyword_success_withoutBuilding() {
		// given
		String memberId = "testMember";
		String keyword = "매칭안되는검색어";
		Member member = createDummyMember();
		RecentSearch recentSearch = createDummyRecentSearch(member, keyword, null);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		doNothing().when(recentSearchRepository).deleteByMemberAndKeyword(member, keyword);
		when(recentSearchRepository.findByMemberOrderBySearchedAtDesc(member)).thenReturn(
			new ArrayList<>());
		when(buildingAliasRepository.findFirstByAliasContainingOrderByIdAsc(keyword))
			.thenReturn(Optional.empty());
		when(recentSearchRepository.save(any(RecentSearch.class))).thenReturn(recentSearch);

		// when
		assertDoesNotThrow(() -> recentSearchService.saveSearchKeyword(memberId, keyword));

		// then
		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository).deleteByMemberAndKeyword(member, keyword);
		verify(recentSearchRepository).flush();
		verify(recentSearchRepository).findByMemberOrderBySearchedAtDesc(member);
		verify(buildingAliasRepository).findFirstByAliasContainingOrderByIdAsc(keyword);
		verify(recentSearchRepository).save(any(RecentSearch.class));
	}

	@DisplayName("검색어저장_성공_기존검색어삭제후저장")
	@Test
	void saveSearchKeyword_success_deleteExistingKeyword() {
		// given
		String memberId = "testMember";
		String keyword = "테스트건물";
		Member member = createDummyMember();
		RecentSearch newSearch = createDummyRecentSearch(member, keyword, null);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		doNothing().when(recentSearchRepository).deleteByMemberAndKeyword(member, keyword);
		when(recentSearchRepository.findByMemberOrderBySearchedAtDesc(member)).thenReturn(
			new ArrayList<>());
		when(buildingAliasRepository.findFirstByAliasContainingOrderByIdAsc(keyword))
			.thenReturn(Optional.empty());
		when(recentSearchRepository.save(any(RecentSearch.class))).thenReturn(newSearch);

		// when
		assertDoesNotThrow(() -> recentSearchService.saveSearchKeyword(memberId, keyword));

		// then
		verify(recentSearchRepository).deleteByMemberAndKeyword(member, keyword);
		verify(recentSearchRepository).flush();
		verify(recentSearchRepository).save(any(RecentSearch.class));
	}

	@DisplayName("검색어저장_성공_10개초과시가장오래된것삭제")
	@Test
	void saveSearchKeyword_success_deleteOldestWhenOver10() {
		// given
		String memberId = "testMember";
		String keyword = "새검색어";
		Member member = createDummyMember();
		List<RecentSearch> existingSearches = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			existingSearches.add(createDummyRecentSearch(member, "검색어" + i, null));
		}
		RecentSearch oldestSearch = existingSearches.get(9);
		RecentSearch newSearch = createDummyRecentSearch(member, keyword, null);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		doNothing().when(recentSearchRepository).deleteByMemberAndKeyword(member, keyword);
		when(recentSearchRepository.findByMemberOrderBySearchedAtDesc(member)).thenReturn(
			existingSearches);
		when(buildingAliasRepository.findFirstByAliasContainingOrderByIdAsc(keyword))
			.thenReturn(Optional.empty());
		when(recentSearchRepository.save(any(RecentSearch.class))).thenReturn(newSearch);
		doNothing().when(recentSearchRepository).delete(oldestSearch);

		// when
		assertDoesNotThrow(() -> recentSearchService.saveSearchKeyword(memberId, keyword));

		// then
		verify(recentSearchRepository).flush();
		verify(recentSearchRepository).delete(oldestSearch);
		verify(recentSearchRepository).save(any(RecentSearch.class));
	}

	@DisplayName("검색어저장_성공_9개일때삭제안함")
	@Test
	void saveSearchKeyword_success_noDeleteWhenUnder10() {
		// given
		String memberId = "testMember";
		String keyword = "새검색어";
		Member member = createDummyMember();
		List<RecentSearch> existingSearches = new ArrayList<>();
		for (int i = 0; i < 9; i++) {
			existingSearches.add(createDummyRecentSearch(member, "검색어" + i, null));
		}
		RecentSearch newSearch = createDummyRecentSearch(member, keyword, null);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		doNothing().when(recentSearchRepository).deleteByMemberAndKeyword(member, keyword);
		when(recentSearchRepository.findByMemberOrderBySearchedAtDesc(member)).thenReturn(
			existingSearches);
		when(buildingAliasRepository.findFirstByAliasContainingOrderByIdAsc(keyword))
			.thenReturn(Optional.empty());
		when(recentSearchRepository.save(any(RecentSearch.class))).thenReturn(newSearch);

		// when
		assertDoesNotThrow(() -> recentSearchService.saveSearchKeyword(memberId, keyword));

		// then
		verify(recentSearchRepository).flush();
		verify(recentSearchRepository, never()).delete(any(RecentSearch.class));
		verify(recentSearchRepository).save(any(RecentSearch.class));
	}

	@DisplayName("검색어저장_실패_회원없음")
	@Test
	void saveSearchKeyword_fail_memberNotFound() {
		// given
		String memberId = "nonExistentMember";
		String keyword = "테스트건물";

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> recentSearchService.saveSearchKeyword(memberId, keyword));

		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository, never()).deleteByMemberAndKeyword(any(), any());
		verify(recentSearchRepository, never()).save(any());
	}

	@DisplayName("최근검색어조회_성공")
	@Test
	void getRecentSearches_success() {
		// given
		String memberId = "testMember";
		Member member = createDummyMember();
		Building building = createDummyBuilding();
		List<RecentSearch> recentSearches = List.of(
			createDummyRecentSearch(member, "검색어1", building),
			createDummyRecentSearch(member, "검색어2", null)
		);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(recentSearchRepository.findByMemberOrderBySearchedAtDesc(member)).thenReturn(
			recentSearches);

		// when
		List<RecentSearchResponseDto> result = recentSearchService.getRecentSearches(memberId);

		// then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("검색어1", result.get(0).getKeyword());
		assertEquals("검색어2", result.get(1).getKeyword());
		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository).findByMemberOrderBySearchedAtDesc(member);
	}

	@DisplayName("최근검색어조회_성공_빈목록")
	@Test
	void getRecentSearches_success_emptyList() {
		// given
		String memberId = "testMember";
		Member member = createDummyMember();
		List<RecentSearch> recentSearches = new ArrayList<>();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(recentSearchRepository.findByMemberOrderBySearchedAtDesc(member)).thenReturn(
			recentSearches);

		// when
		List<RecentSearchResponseDto> result = recentSearchService.getRecentSearches(memberId);

		// then
		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository).findByMemberOrderBySearchedAtDesc(member);
	}

	@DisplayName("최근검색어조회_실패_회원없음")
	@Test
	void getRecentSearches_fail_memberNotFound() {
		// given
		String memberId = "nonExistentMember";

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> recentSearchService.getRecentSearches(memberId));

		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository, never()).findByMemberOrderBySearchedAtDesc(any());
	}

	@DisplayName("검색어단건삭제_성공")
	@Test
	void deleteSearchKeywordByBuildingId_success() {
		// given
		String memberId = "testMember";
		Long buildingId = 1L;
		Member member = createDummyMember();
		Building building = createDummyBuilding();
		RecentSearch recentSearch = createDummyRecentSearch(member, "테스트건물", building);
		List<RecentSearch> searchList = List.of(recentSearch);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(recentSearchRepository.findByMemberAndBuildingId(member, buildingId)).thenReturn(
			searchList);
		doNothing().when(recentSearchRepository).delete(recentSearch);

		// when
		assertDoesNotThrow(
			() -> recentSearchService.deleteSearchKeywordByBuildingId(memberId, buildingId));

		// then
		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository).findByMemberAndBuildingId(member, buildingId);
		verify(recentSearchRepository).delete(recentSearch);
	}

	@DisplayName("검색어단건삭제_성공_여러개중첫번째삭제")
	@Test
	void deleteSearchKeywordByBuildingId_success_deleteFirst() {
		// given
		String memberId = "testMember";
		Long buildingId = 1L;
		Member member = createDummyMember();
		Building building = createDummyBuilding();
		RecentSearch search1 = createDummyRecentSearch(member, "테스트건물1", building);
		RecentSearch search2 = createDummyRecentSearch(member, "테스트건물2", building);
		List<RecentSearch> searchList = List.of(search1, search2);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(recentSearchRepository.findByMemberAndBuildingId(member, buildingId)).thenReturn(
			searchList);
		doNothing().when(recentSearchRepository).delete(search1);

		// when
		assertDoesNotThrow(
			() -> recentSearchService.deleteSearchKeywordByBuildingId(memberId, buildingId));

		// then
		verify(recentSearchRepository).delete(search1);
		verify(recentSearchRepository, never()).delete(search2);
	}

	@DisplayName("검색어단건삭제_실패_회원없음")
	@Test
	void deleteSearchKeywordByBuildingId_fail_memberNotFound() {
		// given
		String memberId = "nonExistentMember";
		Long buildingId = 1L;

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> recentSearchService.deleteSearchKeywordByBuildingId(memberId, buildingId));

		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository, never()).findByMemberAndBuildingId(any(), any());
		verify(recentSearchRepository, never()).delete(any());
	}

	@DisplayName("검색어단건삭제_실패_검색어없음")
	@Test
	void deleteSearchKeywordByBuildingId_fail_searchNotFound() {
		// given
		String memberId = "testMember";
		Long buildingId = 999L;
		Member member = createDummyMember();
		List<RecentSearch> searchList = new ArrayList<>();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(recentSearchRepository.findByMemberAndBuildingId(member, buildingId)).thenReturn(
			searchList);

		// when, then
		assertThrows(InvalidArgumentException.class,
			() -> recentSearchService.deleteSearchKeywordByBuildingId(memberId, buildingId));

		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository).findByMemberAndBuildingId(member, buildingId);
		verify(recentSearchRepository, never()).delete(any());
	}

	@DisplayName("검색어전체삭제_성공")
	@Test
	void deleteAllSearchKeywords_success() {
		// given
		String memberId = "testMember";
		Member member = createDummyMember();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		doNothing().when(recentSearchRepository).deleteByMember(member);

		// when
		assertDoesNotThrow(() -> recentSearchService.deleteAllSearchKeywords(memberId));

		// then
		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository).deleteByMember(member);
	}

	@DisplayName("검색어전체삭제_실패_회원없음")
	@Test
	void deleteAllSearchKeywords_fail_memberNotFound() {
		// given
		String memberId = "nonExistentMember";

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> recentSearchService.deleteAllSearchKeywords(memberId));

		verify(memberRepository).findByMemberId(memberId);
		verify(recentSearchRepository, never()).deleteByMember(any());
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

	private BuildingAlias createDummyBuildingAlias(Building building, String alias) {
		return new BuildingAlias(1L, building, alias);
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

