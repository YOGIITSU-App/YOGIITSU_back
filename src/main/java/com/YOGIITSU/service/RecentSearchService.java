package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.RecentSearchResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.RecentSearch;
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

	//최근 검색어 저장
	@Transactional
	public void saveSearchKeyword(String memberId, String keyword) {
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

		// 기존에 같은 검색어가 있으면 삭제
		recentSearchRepository.deleteByMemberAndKeyword(member, keyword);

		// 최대 개수(6개) 초과 시 가장 오래된 검색어 삭제
		List<RecentSearch> recentSearches = recentSearchRepository.findByMemberOrderBySearchedAtDesc(
			member);
		if (recentSearches.size() >= 6) {
			recentSearchRepository.delete(recentSearches.getLast());
		}

		// 새로운 검색어 저장
		RecentSearch search = RecentSearch.builder()
			.member(member)
			.keyword(keyword)
			.searchedAt(LocalDateTime.now())
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
}