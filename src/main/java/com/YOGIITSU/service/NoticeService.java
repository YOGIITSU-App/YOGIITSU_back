package com.YOGIITSU.service;

import com.YOGIITSU.exception.auth.AdminAccessDeniedException;
import com.YOGIITSU.exception.resource.NoticeNotFoundException;
import com.YOGIITSU.exception.user.AdminNotFoundException;
import com.YOGIITSU.dto.RequestDto.NoticeRequestDto;
import com.YOGIITSU.dto.ResponseDto.NoticeDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.NoticeListResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.Notice;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

	private final NoticeRepository noticeRepository;
	private final MemberRepository memberRepository;
	private final FcmService fcmService;

	// 관리자 권한이 있는지 확인
	private void validateAdmin(Member member) {
		if (!"ADMIN".equalsIgnoreCase(member.getRole())) {
			throw new AdminAccessDeniedException();
		}
	}

	// 공지사항 ID로 공지사항을 조회
	private Notice getNoticeOrThrow(Long id) {
		return noticeRepository.findById(id)
			.orElseThrow(() -> new NoticeNotFoundException(id));
	}

	// 사용자 ID로 회원 정보를 조회
	private Member getMemberOrThrow(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new AdminNotFoundException(memberId));
	}

	// 공지사항 등록
	@Transactional
	public void createNotice(NoticeRequestDto dto, Long memberId) {
		Member member = getMemberOrThrow(memberId);
		validateAdmin(member);

		Notice notice = Notice.builder()
			.noticeTitle(dto.getTitle())
			.noticeContent(dto.getContent())
			.member(member)
			.build();
		noticeRepository.save(notice);

		Long noticeId = notice.getNoticeId();
		String noticeTitle = notice.getNoticeTitle();
		String noticeContent = notice.getNoticeContent();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					fcmService.sendNoticeNotification(noticeTitle, noticeContent, noticeId);
				} catch (Throwable t) {
					log.warn("FCM 알림 전송 실패 (공지는 저장됨) noticeId={}", noticeId, t);
				}
			}
		});
	}

	// 공지사항 수정
	@Transactional
	public void updateNotice(Long id, NoticeRequestDto dto, Long memberId) {
		Member member = getMemberOrThrow(memberId);
		validateAdmin(member);

		Notice notice = getNoticeOrThrow(id);
		notice.update(dto.getTitle(), dto.getContent());
		noticeRepository.save(notice);
	}

	// 공지사항 삭제
	@Transactional
	public void deleteNotice(Long id, Long memberId) {
		Member member = getMemberOrThrow(memberId);
		validateAdmin(member);

		Notice notice = getNoticeOrThrow(id);
		noticeRepository.delete(notice);
	}

	// 공지사항 전체 조회 (최신순 정렬)
	public List<NoticeListResponseDto> getAllNotices() {
		return noticeRepository.findAllByOrderByNoticeAtDesc().stream()
			.map(n -> new NoticeListResponseDto(n.getNoticeId(), n.getNoticeTitle(),
				n.getNoticeAt()))
			.collect(Collectors.toList());
	}

	// 공지사항 단건 조회
	public NoticeDetailResponseDto getNoticeById(Long id) {
		Notice n = getNoticeOrThrow(id);
		return new NoticeDetailResponseDto(n.getNoticeId(), n.getNoticeTitle(),
			n.getNoticeContent(), n.getNoticeAt());
	}
}
