package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.NoticeRequestDto;
import com.YOGIITSU.dto.ResponseDto.NoticeDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.NoticeListResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.Notice;
import com.YOGIITSU.exception.auth.AdminAccessDeniedException;
import com.YOGIITSU.exception.resource.NoticeNotFoundException;
import com.YOGIITSU.exception.user.AdminNotFoundException;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

	@Mock
	private NoticeRepository noticeRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private NoticeService noticeService;

	@Test
	@DisplayName("공지사항_전체_조회_성공")
	void getAllNotices_success() {
		// given
		LocalDateTime now = LocalDateTime.now();
		Notice notice1 = Notice.builder()
			.noticeId(1L)
			.noticeTitle("공지사항 1")
			.noticeAt(now)
			.build();

		Notice notice2 = Notice.builder()
			.noticeId(2L)
			.noticeTitle("공지사항 2")
			.noticeAt(now.minusHours(1))
			.build();

		List<Notice> notices = List.of(notice1, notice2);
		when(noticeRepository.findAllByOrderByNoticeAtDesc()).thenReturn(notices);

		// when
		List<NoticeListResponseDto> result = noticeService.getAllNotices();

		// then
		assertEquals(2, result.size());
		assertEquals(1L, result.get(0).getNoticeId());
		assertEquals("공지사항 1", result.get(0).getNoticeTitle());
		assertEquals(2L, result.get(1).getNoticeId());
		assertEquals("공지사항 2", result.get(1).getNoticeTitle());

		verify(noticeRepository).findAllByOrderByNoticeAtDesc();
	}

	@Test
	@DisplayName("공지사항_단건_조회_성공")
	void getNoticeById_success() {
		// given
		Long noticeId = 1L;
		LocalDateTime now = LocalDateTime.now();
		Notice notice = Notice.builder()
			.noticeId(noticeId)
			.noticeTitle("테스트 공지사항")
			.noticeContent("테스트 내용")
			.noticeAt(now)
			.build();

		when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(notice));

		// when
		NoticeDetailResponseDto result = noticeService.getNoticeById(noticeId);

		// then
		assertEquals(noticeId, result.getNoticeId());
		assertEquals("테스트 공지사항", result.getNoticeTitle());
		assertEquals("테스트 내용", result.getNoticeContent());
		assertEquals(now, result.getNoticeAt());

		verify(noticeRepository).findById(noticeId);
	}

	@Test
	@DisplayName("공지사항_단건_조회_실패_공지사항없음")
	void getNoticeById_fail_noticeNotFound() {
		// given
		Long noticeId = 999L;
		when(noticeRepository.findById(noticeId)).thenReturn(Optional.empty());

		// when & then
		assertThrows(NoticeNotFoundException.class, () -> noticeService.getNoticeById(noticeId));
		verify(noticeRepository).findById(noticeId);
	}

	@Test
	@DisplayName("공지사항_등록_성공")
	void createNotice_success() {
		// given
		Long memberId = 1L;
		NoticeRequestDto dto = new NoticeRequestDto();
		dto.setTitle("새 공지사항");
		dto.setContent("새 내용");

		Member adminMember = Member.builder()
			.id(memberId)
			.role("ADMIN")
			.build();

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(adminMember));
		when(noticeRepository.save(any(Notice.class))).thenReturn(Notice.builder().build());

		// when
		noticeService.createNotice(dto, memberId);

		// then
		verify(memberRepository).findById(memberId);
		verify(noticeRepository).save(any(Notice.class));
	}

	@Test
	@DisplayName("공지사항_등록_실패_관리자아님")
	void createNotice_fail_notAdmin() {
		// given
		Long memberId = 1L;
		NoticeRequestDto dto = new NoticeRequestDto();
		dto.setTitle("새 공지사항");
		dto.setContent("새 내용");

		Member normalMember = Member.builder()
			.id(memberId)
			.role("USER")
			.build();

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(normalMember));

		// when & then
		assertThrows(AdminAccessDeniedException.class,
			() -> noticeService.createNotice(dto, memberId));
		verify(memberRepository).findById(memberId);
		verify(noticeRepository, never()).save(any(Notice.class));
	}

	@Test
	@DisplayName("공지사항_등록_실패_회원없음")
	void createNotice_fail_memberNotFound() {
		// given
		Long memberId = 999L;
		NoticeRequestDto dto = new NoticeRequestDto();
		dto.setTitle("새 공지사항");
		dto.setContent("새 내용");

		when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

		// when & then
		assertThrows(AdminNotFoundException.class, () -> noticeService.createNotice(dto, memberId));
		verify(memberRepository).findById(memberId);
		verify(noticeRepository, never()).save(any(Notice.class));
	}

	@Test
	@DisplayName("공지사항_수정_성공")
	void updateNotice_success() {
		// given
		Long noticeId = 1L;
		Long memberId = 1L;
		NoticeRequestDto dto = new NoticeRequestDto();
		dto.setTitle("수정된 제목");
		dto.setContent("수정된 내용");

		Member adminMember = Member.builder()
			.id(memberId)
			.role("ADMIN")
			.build();

		Notice existingNotice = Notice.builder()
			.noticeId(noticeId)
			.noticeTitle("기존 제목")
			.noticeContent("기존 내용")
			.build();

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(adminMember));
		when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(existingNotice));
		when(noticeRepository.save(any(Notice.class))).thenReturn(existingNotice);

		// when
		noticeService.updateNotice(noticeId, dto, memberId);

		// then
		verify(memberRepository).findById(memberId);
		verify(noticeRepository).findById(noticeId);
		verify(noticeRepository).save(existingNotice);
	}

	@Test
	@DisplayName("공지사항_수정_실패_공지사항없음")
	void updateNotice_fail_noticeNotFound() {
		// given
		Long noticeId = 999L;
		Long memberId = 1L;
		NoticeRequestDto dto = new NoticeRequestDto();
		dto.setTitle("수정된 제목");
		dto.setContent("수정된 내용");

		Member adminMember = Member.builder()
			.id(memberId)
			.role("ADMIN")
			.build();

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(adminMember));
		when(noticeRepository.findById(noticeId)).thenReturn(Optional.empty());

		// when & then
		assertThrows(NoticeNotFoundException.class,
			() -> noticeService.updateNotice(noticeId, dto, memberId));
		verify(memberRepository).findById(memberId);
		verify(noticeRepository).findById(noticeId);
		verify(noticeRepository, never()).save(any(Notice.class));
	}

	@Test
	@DisplayName("공지사항_삭제_성공")
	void deleteNotice_success() {
		// given
		Long noticeId = 1L;
		Long memberId = 1L;

		Member adminMember = Member.builder()
			.id(memberId)
			.role("ADMIN")
			.build();

		Notice existingNotice = Notice.builder()
			.noticeId(noticeId)
			.noticeTitle("삭제할 공지사항")
			.build();

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(adminMember));
		when(noticeRepository.findById(noticeId)).thenReturn(Optional.of(existingNotice));

		// when
		noticeService.deleteNotice(noticeId, memberId);

		// then
		verify(memberRepository).findById(memberId);
		verify(noticeRepository).findById(noticeId);
		verify(noticeRepository).delete(existingNotice);
	}

	@Test
	@DisplayName("공지사항_삭제_실패_공지사항없음")
	void deleteNotice_fail_noticeNotFound() {
		// given
		Long noticeId = 999L;
		Long memberId = 1L;

		Member adminMember = Member.builder()
			.id(memberId)
			.role("ADMIN")
			.build();

		when(memberRepository.findById(memberId)).thenReturn(Optional.of(adminMember));
		when(noticeRepository.findById(noticeId)).thenReturn(Optional.empty());

		// when & then
		assertThrows(NoticeNotFoundException.class,
			() -> noticeService.deleteNotice(noticeId, memberId));
		verify(memberRepository).findById(memberId);
		verify(noticeRepository).findById(noticeId);
		verify(noticeRepository, never()).delete(any(Notice.class));
	}
}