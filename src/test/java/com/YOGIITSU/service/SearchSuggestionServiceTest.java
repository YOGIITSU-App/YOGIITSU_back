package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.SearchSuggestionResponseDto;
import com.YOGIITSU.entity.*;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.repository.BuildingAliasRepository;
import com.YOGIITSU.repository.BuildingRepository;
import com.YOGIITSU.repository.FavoriteRepository;
import com.YOGIITSU.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchSuggestionServiceTest {

	@Mock
	private BuildingRepository buildingRepository;

	@Mock
	private BuildingAliasRepository buildingAliasRepository;

	@Mock
	private FavoriteRepository favoriteRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private SearchSuggestionService searchSuggestionService;

	@DisplayName("자동완성_성공_회원_즐겨찾기우선_중복제거")
	@Test
	void getSearchSuggestions_success_withMember_bookmarkPriority() {
		// given
		String query = "대학";
		String memberId = "member1";
		Member member = createDummyMember(memberId);

		Building buildingFav = createBuilding(1L, "대학본관", "태그1");
		Building buildingSearch = createBuilding(2L, "대학도서관", "태그2");
		Building buildingAlias = createBuilding(3L, "공학관", "태그3");

		Favorite favorite = Favorite.builder()
			.id(10L)
			.member(member)
			.building(buildingFav)
			.build();

		// 즐겨찾기 중 검색어를 포함하지 않는 건물은 필터링되어 제외되어야 함
		Building buildingFavNoMatch = createBuilding(4L, "체육관", "태그4");
		Favorite favoriteNoMatch = Favorite.builder()
			.id(11L)
			.member(member)
			.building(buildingFavNoMatch)
			.build();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(favoriteRepository.findByMember(member)).thenReturn(
			List.of(favorite, favoriteNoMatch));
		when(buildingRepository.findTop6ByNameContainingOrderByNameAsc(query))
			.thenReturn(new ArrayList<>(List.of(buildingFav, buildingSearch)));
		when(buildingAliasRepository.findByAliasContaining(query))
			.thenReturn(List.of(new BuildingAlias(100L, buildingAlias, "공학관 별칭")));

		// when
		List<SearchSuggestionResponseDto> result =
			searchSuggestionService.getSearchSuggestions(query, memberId);

		// then
		assertNotNull(result);
		assertEquals(3, result.size()); // 즐겨찾기 1개 + 일반 2개(중복 제거)
		assertTrue(result.get(0).isBookmarked()); // 즐겨찾기 우선
		assertEquals(buildingFav.getId(), result.get(0).getBuildingId());
		assertFalse(result.get(1).isBookmarked());
		assertFalse(result.get(2).isBookmarked());

		verify(memberRepository).findByMemberId(memberId);
		verify(favoriteRepository).findByMember(member);
		verify(buildingRepository).findTop6ByNameContainingOrderByNameAsc(query);
		verify(buildingAliasRepository).findByAliasContaining(query);
	}

	@DisplayName("자동완성_성공_비회원_즐겨찾기없음")
	@Test
	void getSearchSuggestions_success_guest() {
		// given
		String query = "도서";

		Building building1 = createBuilding(1L, "도서관", "태그A");
		Building building2 = createBuilding(2L, "도서정보관", "태그B");
		Building buildingAlias = createBuilding(3L, "자료실", "태그C");

		when(buildingRepository.findTop6ByNameContainingOrderByNameAsc(query))
			.thenReturn(new ArrayList<>(List.of(building1, building2)));
		when(buildingAliasRepository.findByAliasContaining(query))
			.thenReturn(List.of(new BuildingAlias(200L, buildingAlias, "자료실 별칭")));

		// when
		List<SearchSuggestionResponseDto> result =
			searchSuggestionService.getSearchSuggestions(query, null);

		// then
		assertNotNull(result);
		assertEquals(3, result.size());
		assertTrue(result.stream().allMatch(dto -> !dto.isBookmarked()));
		verify(memberRepository, never()).findByMemberId(any());
		verify(favoriteRepository, never()).findByMember(any());
	}

	@DisplayName("자동완성_실패_회원없음")
	@Test
	void getSearchSuggestions_fail_memberNotFound() {
		// given
		String query = "대학";
		String memberId = "missing";
		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> searchSuggestionService.getSearchSuggestions(query, memberId));

		verify(memberRepository).findByMemberId(memberId);
		verify(favoriteRepository, never()).findByMember(any());
	}

	@DisplayName("자동완성_성공_6개초과시_6개로제한_즐겨찾기우선")
	@Test
	void getSearchSuggestions_success_limitSix() {
		// given
		String query = "빌딩";
		String memberId = "member1";
		Member member = createDummyMember(memberId);

		List<Favorite> favorites = new ArrayList<>();
		for (long i = 1; i <= 3; i++) {
			Building b = createBuilding(i, "빌딩" + i, "F" + i);
			favorites.add(Favorite.builder().id(300L + i).member(member).building(b).build());
		}

		List<Building> searchResults = new ArrayList<>();
		for (long i = 4; i <= 10; i++) { // 7개
			searchResults.add(createBuilding(i, "빌딩" + i, "S" + i));
		}

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(favoriteRepository.findByMember(member)).thenReturn(favorites);
		when(buildingRepository.findTop6ByNameContainingOrderByNameAsc(query))
			.thenReturn(searchResults);
		when(buildingAliasRepository.findByAliasContaining(query)).thenReturn(
			Collections.emptyList());

		// when
		List<SearchSuggestionResponseDto> result =
			searchSuggestionService.getSearchSuggestions(query, memberId);

		// then
		assertEquals(6, result.size());
		// 즐겨찾기 3개 우선 + 검색 결과 3개
		for (int i = 0; i < 3; i++) {
			assertTrue(result.get(i).isBookmarked());
		}
		for (int i = 3; i < 6; i++) {
			assertFalse(result.get(i).isBookmarked());
		}
	}

	private Member createDummyMember(String memberId) {
		return Member.builder()
			.id(1L)
			.memberId(memberId)
			.password("pw")
			.email("test@example.com")
			.userName("user")
			.role("USER")
			.joinAt(LocalDateTime.now())
			.provider("local")
			.build();
	}

	private Building createBuilding(Long id, String name, String tagName) {
		Building building = Building.builder()
			.id(id)
			.name(name)
			.latitude(0.0)
			.longitude(0.0)
			.imageUrl("img")
			.build();

		BuildingTag tag = BuildingTag.builder()
			.id(id * 10)
			.building(building)
			.name(tagName)
			.build();

		building.getBuildingTags().add(tag);
		return building;
	}
}

