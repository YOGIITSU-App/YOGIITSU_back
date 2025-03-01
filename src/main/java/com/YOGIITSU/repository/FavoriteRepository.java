package com.YOGIITSU.repository;

import com.YOGIITSU.entity.Favorite;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

	// 사용자 ID로 즐겨찾기 목록 조회
	List<Favorite> findByMember(Member member);

	// 사용자 ID와 건물 ID로 즐겨찾기 존재 여부 확인
	boolean existsByMemberAndBuilding(Member member, Building building);

	// 사용자 ID와 건물 ID로 즐겨찾기 삭제
	void deleteByMemberAndBuilding(Member member, Building building);
}