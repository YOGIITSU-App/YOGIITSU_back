package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Building;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.RecentSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {

	// 최근 검색어 조회
	List<RecentSearch> findByMemberOrderBySearchedAtDesc(Member member);

	// 검색어 삭제
	void deleteByMemberAndKeyword(Member member, String keyword);
	// 특정 회원과 건물에 대한 검색어 조회
	List<RecentSearch> findByMemberAndBuildingId(Member member, Long buildingId);

}