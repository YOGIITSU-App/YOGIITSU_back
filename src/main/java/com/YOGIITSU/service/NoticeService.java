package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.NoticeRequestDto;
import com.YOGIITSU.dto.ResponseDto.NoticeResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.entity.Notice;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    // 관리자 권한이 있는지 확인
    private void validateAdmin(Member member) {
        if (!"ADMIN".equalsIgnoreCase(member.getRole())) {
            throw new SecurityException("관리자만 수행할 수 있는 작업입니다.");
        }
    }

    // 공지사항 ID로 공지사항을 조회
    private Notice getNoticeOrThrow(Long id) {
        return noticeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));
    }

    // 사용자 ID로 회원 정보를 조회
    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("해당 관리자 계정을 찾을 수 없습니다."));
    }

    // 공지사항 등록
    @Transactional
    public void createNotice(NoticeRequestDto dto, Long memberId) {
        Member member = getMemberOrThrow(memberId);
        validateAdmin(member);

        Notice notice = Notice.builder()
            .title(dto.getTitle())
            .content(dto.getContent())
            .member(member)
            .build();
        noticeRepository.save(notice);
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
    public List<NoticeResponseDto> getAllNotices() {
        return noticeRepository.findAllByOrderByNoticeAtDesc().stream()
            .map(n -> new NoticeResponseDto(n.getId(), n.getTitle(), n.getContent(),
                n.getNoticeAt()))
            .collect(Collectors.toList());
    }

    // 공지사항 단건 조회
    public NoticeResponseDto getNoticeById(Long id) {
        Notice n = noticeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 존재하지 않습니다."));
        return new NoticeResponseDto(n.getId(), n.getTitle(), n.getContent(), n.getNoticeAt());
    }
}