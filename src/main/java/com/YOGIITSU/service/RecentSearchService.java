package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.RecentSearchResponseDto;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.BuildingAlias;
import com.YOGIITSU.entity.RecentSearch;
import com.YOGIITSU.repository.BuildingAliasRepository;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.repository.RecentSearchRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecentSearchService {

	private final RecentSearchRepository recentSearchRepository;
	private final MemberRepository memberRepository;
	private final BuildingAliasRepository buildingAliasRepository;

	//최근 검색어 저장
	@Transactional
	public void saveSearchKeyword(String memberId, String keyword) {
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		// 기존에 같은 검색어가 있으면 삭제
		recentSearchRepository.deleteByMemberAndKeyword(member, keyword);
		recentSearchRepository.flush();

		// 사용자별 최대 10개까지만 유지
		List<RecentSearch> recentSearches = recentSearchRepository.findByMemberOrderBySearchedAtDesc(
			member);
		if (recentSearches.size() >= 10) {
			recentSearchRepository.delete(recentSearches.getLast());
		}

		// alias 기반으로 building 매핑
		Building matchedBuilding = buildingAliasRepository
			.findFirstByAliasContainingOrderByIdAsc(keyword)
			.map(BuildingAlias::getBuilding)
			.orElse(null);

		// 새로운 검색어 저장
		RecentSearch search = RecentSearch.builder()
			.member(member)
			.keyword(keyword)
			.searchedAt(LocalDateTime.now())
			.building(matchedBuilding)
			.build();
		recentSearchRepository.save(search);
	}

	// 최근 검색어 조회
	@Transactional(readOnly = true)
	public List<RecentSearchResponseDto> getRecentSearches(String memberId) {
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		return recentSearchRepository.findByMemberOrderBySearchedAtDesc(member)
			.stream()
			.map(RecentSearchResponseDto::new)
			.toList();
	}

	// 검색어 단건 삭제
	@Transactional
	public void deleteSearchKeywordByBuildingId(String memberId, Long buildingId) {
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		List<RecentSearch> searchList = recentSearchRepository.findByMemberAndBuildingId(member,
			buildingId);
		if (searchList.isEmpty()) {
			throw new EntityNotFoundException("해당 건물과 연결된 검색어가 없습니다.");
		}

		// 가장 최근 항목 하나만 삭제
		recentSearchRepository.delete(searchList.get(0));
	}

	// 검색어 전체 삭제
	@Transactional
	public void deleteAllSearchKeywords(String memberId) {
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
		recentSearchRepository.deleteByMember(member);
	}
}