package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.SearchSuggestionResponseDto;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.entity.BuildingAlias;
import com.YOGIITSU.entity.BuildingTag;
import com.YOGIITSU.entity.Favorite;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.BuildingAliasRepository;
import com.YOGIITSU.repository.BuildingRepository;
import com.YOGIITSU.repository.FavoriteRepository;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchSuggestionService {

	private final BuildingRepository buildingRepository;
	private final BuildingAliasRepository buildingAliasRepository;
	private final FavoriteRepository favoriteRepository;
	private final MemberRepository memberRepository;

	public List<SearchSuggestionResponseDto> getSearchSuggestions(String query, String memberId) {
		// 1. memberId가 null이면 비회원이므로 즐겨찾기 건물은 빈 리스트
		List<Building> bookmarkedBuildings = new ArrayList<>();
		if (memberId != null) {
			Member member = findMemberById(memberId);
			bookmarkedBuildings = findBookmarkedBuildings(member, query);
		}

		// 2. DB에서 검색어가 포함된 건물 리스트 가져오기
		List<Building> searchResults = findBuildingsWithAliases(query);

		// 3. 즐겨찾기된 건물 리스트 + 일반 검색 결과 리스트 합치기
		return mergeResults(bookmarkedBuildings, searchResults);
	}

	private Member findMemberById(String memberId) {
		return memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId));
	}

	private List<Building> findBookmarkedBuildings(Member member, String query) {
		return favoriteRepository.findByMember(member).stream()
			.map(Favorite::getBuilding)
			.filter(building -> building.getName().contains(query)) // 즐겨찾기 중 검색어 포함된 것 필터링
			.toList();
	}

	private List<Building> findBuildingsWithAliases(String query) {
		// 1. 공식 명칭 기반 검색
		List<Building> buildings = buildingRepository.findTop6ByNameContainingOrderByNameAsc(query);

		// 2. 별칭 기반 검색
		List<BuildingAlias> aliases = buildingAliasRepository.findByAliasContaining(query);
		List<Building> aliasBuildings = aliases.stream()
			.map(BuildingAlias::getBuilding)
			.distinct()
			.toList();

		// 3. 기존 목록에 없는 별칭 기반 건물 추가
		Set<Long> existingIds = buildings.stream().map(Building::getId).collect(Collectors.toSet());
		for (Building aliasBuilding : aliasBuildings) {
			if (!existingIds.contains(aliasBuilding.getId())) {
				buildings.add(aliasBuilding);
			}
		}
		return buildings;
	}

	private List<SearchSuggestionResponseDto> mergeResults(List<Building> bookmarkedBuildings,
		List<Building> searchResults) {
		Set<Long> bookmarkedBuildingIds = bookmarkedBuildings.stream()
			.map(Building::getId)
			.collect(Collectors.toSet());

		List<SearchSuggestionResponseDto> finalResults = new ArrayList<>();

		// 즐겨찾기된 건물 먼저 추가
		for (Building building : bookmarkedBuildings) {
			finalResults.add(new SearchSuggestionResponseDto(
				building.getId(),
				building.getName(),
				true,
				building.getBuildingTags().stream()
					.map(BuildingTag::getName)
					.collect(Collectors.toList())
			));
		}

		// 일반 검색 결과에서 중복되지 않는 건물 추가
		for (Building building : searchResults) {
			if (!bookmarkedBuildingIds.contains(building.getId())) {
				finalResults.add(new SearchSuggestionResponseDto(
					building.getId(),
					building.getName(),
					false,
					building.getBuildingTags().stream()
						.map(BuildingTag::getName)
						.collect(Collectors.toList())
				));
			}
		}

		return finalResults.stream()
			.limit(6)
			.collect(Collectors.toList());
	}
}