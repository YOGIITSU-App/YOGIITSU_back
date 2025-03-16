package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.SearchSuggestionResponseDto;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.entity.Favorite;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.BuildingRepository;
import com.YOGIITSU.repository.FavoriteRepository;
import com.YOGIITSU.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchSuggestionService {

	private final BuildingRepository buildingRepository;
	private final FavoriteRepository favoriteRepository;
	private final MemberRepository memberRepository;

	// 자동완성 검색어 추천
	public List<SearchSuggestionResponseDto> getSearchSuggestions(String query, String memberId) {
		// 1. 입력된 검색어가 포함된 단과대 리스트 조회 (최대 6개)
		List<Building> buildings = buildingRepository.findTop6ByNameContainingOrderByNameAsc(query);

		// 2. 현재 사용자의 즐겨찾기 목록 조회
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
		List<Favorite> favorites = favoriteRepository.findByMember(member);
		List<Long> bookmarkedBuildingIds = favorites.stream()
			.map(favorite -> favorite.getBuilding().getId())
			.toList();

		// 3. 검색 결과와 즐겨찾기 여부 매핑
		return buildings.stream()
			.map(building -> new SearchSuggestionResponseDto(
				building.getName(),
				bookmarkedBuildingIds.contains(building.getId()) // 즐겨찾기 여부 확인
			))
			.collect(Collectors.toList());
	}
}