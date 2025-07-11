package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Building;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.RecentSearch;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {

	// 최근 검색어 조회
	List<RecentSearch> findByMemberOrderBySearchedAtDesc(Member member);

	// 검색어 삭제
	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM RecentSearch rs WHERE rs.member = :member AND rs.keyword = :keyword")
	void deleteByMemberAndKeyword(@Param("member") Member member, @Param("keyword") String keyword);


	// 특정 회원의 모든 검색어 삭제
	void deleteByMember(Member member);

	// 특정 회원과 건물에 대한 검색어 조회
	List<RecentSearch> findByMemberAndBuildingId(Member member, Long buildingId);

	// 30일 이전의 검색어 삭제
	int deleteBySearchedAtBefore(LocalDateTime threshold);
}