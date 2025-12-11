package com.YOGIITSU.service;

import com.YOGIITSU.entity.Favorite;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.exception.resource.FavoriteNotFoundException;
import com.YOGIITSU.repository.FavoriteRepository;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.repository.BuildingRepository;
import com.YOGIITSU.exception.resource.FavoriteAlreadyExistsException;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.exception.building.BuildingNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final MemberRepository memberRepository;
	private final BuildingRepository buildingRepository;

	/**
	 * 즐겨찾기 추가
	 *
	 * @param memberId   사용자 ID
	 * @param buildingId 건물 ID
	 */
	@Transactional
	public void addFavorite(String memberId, Long buildingId) {
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId));
		Building building = buildingRepository.findById(buildingId)
			.orElseThrow(() -> new BuildingNotFoundException(buildingId));

		if (favoriteRepository.existsByMemberAndBuilding(member, building)) {
			throw new FavoriteAlreadyExistsException();
		}

		Favorite favorite = Favorite.builder()
			.member(member)
			.building(building)
			.build();
		favoriteRepository.save(favorite);
	}

	/**
	 * 즐겨찾기 삭제
	 *
	 * @param memberId   사용자 ID
	 * @param buildingId 건물 ID
	 */
	@Transactional
	public void removeFavorite(String memberId, Long buildingId) {
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId));
		Building building = buildingRepository.findById(buildingId)
			.orElseThrow(() -> new BuildingNotFoundException(buildingId));

		if (!favoriteRepository.existsByMemberAndBuilding(member, building)) {
			throw new FavoriteNotFoundException();
		}

		favoriteRepository.deleteByMemberAndBuilding(member, building);
	}

	/**
	 * 즐겨찾기 목록 조회
	 *
	 * @param memberId 사용자 ID
	 * @return 즐겨찾기 목록
	 */
	public List<Favorite> getFavorites(String memberId) {
		Member member = memberRepository.findByMemberId(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId));

		return favoriteRepository.findByMember(member);
	}
}
