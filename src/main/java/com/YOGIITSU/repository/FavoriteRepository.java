package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Favorite;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.Building;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

	// 사용자 ID로 즐겨찾기 목록 조회
	List<Favorite> findByMember(Member member);

	// 사용자 ID와 건물 ID로 즐겨찾기 존재 여부 확인
	boolean existsByMemberAndBuilding(Member member, Building building);

	// 사용자 ID와 건물 ID로 즐겨찾기 삭제
	void deleteByMemberAndBuilding(Member member, Building building);

	// 사용자 ID와 건물 ID로 즐겨찾기 존재 여부 확인
	boolean existsByMemberIdAndBuildingId(Long memberId, Long buildingId);

	/**
	 * 특정 사용자가 즐겨찾기한 모든 건물의 ID를 조회 (N+1 문제 해결용)
	 *
	 * @param memberId 사용자의 ID
	 * @return 즐겨찾기한 건물 ID의 Set
	 */
	@Query("SELECT f.building.id FROM Favorite f WHERE f.member.id = :memberId")
	Set<Long> findBuildingIdsByMemberId(@Param("memberId") Long memberId);
}