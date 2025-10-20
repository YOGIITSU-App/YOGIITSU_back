package com.YOGIITSU.service;

import com.YOGIITSU.entity.Favorite;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.Building;
import com.YOGIITSU.exception.resource.FavoriteAlreadyExistsException;
import com.YOGIITSU.exception.resource.FavoriteNotFoundException;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.exception.building.BuildingNotFoundException;
import com.YOGIITSU.repository.FavoriteRepository;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.repository.BuildingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

	@Mock
	private FavoriteRepository favoriteRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private BuildingRepository buildingRepository;

	@InjectMocks
	private FavoriteService favoriteService;

	@DisplayName("즐겨찾기추가_성공")
	@Test
	void addFavorite_success() {
		// given
		String memberId = "testMember";
		Long buildingId = 1L;
		Member member = createDummyMember();
		Building building = createDummyBuilding();
		Favorite favorite = createDummyFavorite(member, building);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
		when(favoriteRepository.existsByMemberAndBuilding(member, building)).thenReturn(false);
		when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);

		// when
		assertDoesNotThrow(() -> favoriteService.addFavorite(memberId, buildingId));

		// then
		verify(memberRepository).findByMemberId(memberId);
		verify(buildingRepository).findById(buildingId);
		verify(favoriteRepository).existsByMemberAndBuilding(member, building);
		verify(favoriteRepository).save(any(Favorite.class));
	}

	@DisplayName("즐겨찾기추가_실패_회원없음")
	@Test
	void addFavorite_fail_memberNotFound() {
		// given
		String memberId = "nonExistentMember";
		Long buildingId = 1L;

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> favoriteService.addFavorite(memberId, buildingId));

		verify(memberRepository).findByMemberId(memberId);
		verify(buildingRepository, never()).findById(any());
		verify(favoriteRepository, never()).existsByMemberAndBuilding(any(), any());
		verify(favoriteRepository, never()).save(any());
	}

	@DisplayName("즐겨찾기추가_실패_건물없음")
	@Test
	void addFavorite_fail_buildingNotFound() {
		// given
		String memberId = "testMember";
		Long buildingId = 999L;
		Member member = createDummyMember();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(BuildingNotFoundException.class,
			() -> favoriteService.addFavorite(memberId, buildingId));

		verify(memberRepository).findByMemberId(memberId);
		verify(buildingRepository).findById(buildingId);
		verify(favoriteRepository, never()).existsByMemberAndBuilding(any(), any());
		verify(favoriteRepository, never()).save(any());
	}

	@DisplayName("즐겨찾기추가_실패_이미존재")
	@Test
	void addFavorite_fail_alreadyExists() {
		// given
		String memberId = "testMember";
		Long buildingId = 1L;
		Member member = createDummyMember();
		Building building = createDummyBuilding();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
		when(favoriteRepository.existsByMemberAndBuilding(member, building)).thenReturn(true);

		// when, then
		assertThrows(FavoriteAlreadyExistsException.class,
			() -> favoriteService.addFavorite(memberId, buildingId));

		verify(memberRepository).findByMemberId(memberId);
		verify(buildingRepository).findById(buildingId);
		verify(favoriteRepository).existsByMemberAndBuilding(member, building);
		verify(favoriteRepository, never()).save(any());
	}

	@DisplayName("즐겨찾기삭제_성공")
	@Test
	void removeFavorite_success() {
		// given
		String memberId = "testMember";
		Long buildingId = 1L;
		Member member = createDummyMember();
		Building building = createDummyBuilding();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
		when(favoriteRepository.existsByMemberAndBuilding(member, building)).thenReturn(true);
		doNothing().when(favoriteRepository).deleteByMemberAndBuilding(member, building);

		// when
		assertDoesNotThrow(() -> favoriteService.removeFavorite(memberId, buildingId));

		// then
		verify(memberRepository).findByMemberId(memberId);
		verify(buildingRepository).findById(buildingId);
		verify(favoriteRepository).existsByMemberAndBuilding(member, building);
		verify(favoriteRepository).deleteByMemberAndBuilding(member, building);
	}

	@DisplayName("즐겨찾기삭제_실패_회원없음")
	@Test
	void removeFavorite_fail_memberNotFound() {
		// given
		String memberId = "nonExistentMember";
		Long buildingId = 1L;

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> favoriteService.removeFavorite(memberId, buildingId));

		verify(memberRepository).findByMemberId(memberId);
		verify(buildingRepository, never()).findById(any());
		verify(favoriteRepository, never()).existsByMemberAndBuilding(any(), any());
		verify(favoriteRepository, never()).deleteByMemberAndBuilding(any(), any());
	}

	@DisplayName("즐겨찾기삭제_실패_건물없음")
	@Test
	void removeFavorite_fail_buildingNotFound() {
		// given
		String memberId = "testMember";
		Long buildingId = 999L;
		Member member = createDummyMember();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(buildingRepository.findById(buildingId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(BuildingNotFoundException.class,
			() -> favoriteService.removeFavorite(memberId, buildingId));

		verify(memberRepository).findByMemberId(memberId);
		verify(buildingRepository).findById(buildingId);
		verify(favoriteRepository, never()).existsByMemberAndBuilding(any(), any());
		verify(favoriteRepository, never()).deleteByMemberAndBuilding(any(), any());
	}

	@DisplayName("즐겨찾기삭제_실패_즐겨찾기없음")
	@Test
	void removeFavorite_fail_favoriteNotFound() {
		// given
		String memberId = "testMember";
		Long buildingId = 1L;
		Member member = createDummyMember();
		Building building = createDummyBuilding();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(buildingRepository.findById(buildingId)).thenReturn(Optional.of(building));
		when(favoriteRepository.existsByMemberAndBuilding(member, building)).thenReturn(false);

		// when, then
		assertThrows(FavoriteNotFoundException.class,
			() -> favoriteService.removeFavorite(memberId, buildingId));

		verify(memberRepository).findByMemberId(memberId);
		verify(buildingRepository).findById(buildingId);
		verify(favoriteRepository).existsByMemberAndBuilding(member, building);
		verify(favoriteRepository, never()).deleteByMemberAndBuilding(any(), any());
	}

	@DisplayName("즐겨찾기목록조회_성공")
	@Test
	void getFavorites_success() {
		// given
		String memberId = "testMember";
		Member member = createDummyMember();
		Building building1 = createDummyBuilding();
		Building building2 = Building.builder()
			.id(2L)
			.name("테스트건물2")
			.latitude(37.123456)
			.longitude(127.123456)
			.imageUrl("test2.jpg")
			.build();

		List<Favorite> expectedFavorites = List.of(
			createDummyFavorite(member, building1),
			createDummyFavorite(member, building2)
		);

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(favoriteRepository.findByMember(member)).thenReturn(expectedFavorites);

		// when
		List<Favorite> result = favoriteService.getFavorites(memberId);

		// then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(expectedFavorites, result);
		verify(memberRepository).findByMemberId(memberId);
		verify(favoriteRepository).findByMember(member);
	}

	@DisplayName("즐겨찾기목록조회_성공_빈목록")
	@Test
	void getFavorites_success_emptyList() {
		// given
		String memberId = "testMember";
		Member member = createDummyMember();
		List<Favorite> expectedFavorites = List.of();

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.of(member));
		when(favoriteRepository.findByMember(member)).thenReturn(expectedFavorites);

		// when
		List<Favorite> result = favoriteService.getFavorites(memberId);

		// then
		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(memberRepository).findByMemberId(memberId);
		verify(favoriteRepository).findByMember(member);
	}

	@DisplayName("즐겨찾기목록조회_실패_회원없음")
	@Test
	void getFavorites_fail_memberNotFound() {
		// given
		String memberId = "nonExistentMember";

		when(memberRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

		// when, then
		assertThrows(MemberNotFoundException.class,
			() -> favoriteService.getFavorites(memberId));

		verify(memberRepository).findByMemberId(memberId);
		verify(favoriteRepository, never()).findByMember(any());
	}

	private Member createDummyMember() {
		return Member.builder()
			.id(1L)
			.memberId("testMember")
			.password("password")
			.email("test@example.com")
			.userName("테스트사용자")
			.role("USER")
			.joinAt(LocalDateTime.now())
			.provider("local")
			.build();
	}

	private Building createDummyBuilding() {
		return Building.builder()
			.id(1L)
			.name("테스트건물")
			.latitude(37.123456)
			.longitude(127.123456)
			.imageUrl("test.jpg")
			.build();
	}

	private Favorite createDummyFavorite(Member member, Building building) {
		return Favorite.builder()
			.id(1L)
			.member(member)
			.building(building)
			.build();
	}
}
