package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
import com.YOGIITSU.dto.RequestDto.InquiryRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryListResponseDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Inquiry;
import com.YOGIITSU.entity.InquiryState;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.InquiryRepository;
import com.YOGIITSU.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * InquiryService
 * - 문의 등록, 조회, 수정, 삭제에 대한 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    /**
     * Create: 문의 등록
     * - 제목과 내용이 비어 있는지 검증
     * - 상태를 '답변 대기'로 설정한 후 저장
     *
     * @param requestDto 문의 요청 데이터
     * @param memberId   문의 작성한 회원 ID
     * @return InquiryResponseDto 등록된 문의 정보
     */
    @Transactional
    public void createInquiry(InquiryRequestDto requestDto, Long memberId) {

        // 1. 회원 조회
        Member member = findMember(memberId);

        // 2. 입력값 유효성 검사
        if (requestDto.getInquiryTitle() == null || requestDto.getInquiryTitle().isEmpty()) {
            throw new IllegalArgumentException("문의 제목을 입력해주세요.");
        }
        if (requestDto.getInquiryContent() == null || requestDto.getInquiryContent().isEmpty()) {
            throw new IllegalArgumentException("문의 내용을 입력해주세요.");
        }

        Inquiry inquiry = Inquiry.builder()
            .member(member)
            .inquiryTitle(requestDto.getInquiryTitle())
            .inquiryContent(requestDto.getInquiryContent())
            .inquiryState(InquiryState.PROCESSING)  // 기본 상태: PROCESSING(답변대기)
            .build();

        // 3. 생성된 문의 DB에 저장
        inquiryRepository.save(inquiry);
    }

    /**
     * Read-list: 문의 전체 리스트 조회
     * - 최신순으로 정렬
     * - 제목, 상태, 작성자, 작성일만 포함된 DTO 리스트 반환
     *
     * @return InquiryListResponseDto 문의 리스트
     */
    @Transactional(readOnly = true)
    public List<InquiryListResponseDto> getAllInquiries() {
        return inquiryRepository.findAllByOrderByInquiryAtDesc().stream()
            .map(InquiryListResponseDto::new)
            .collect(Collectors.toList());
    }

    /**
     * Read: 문의 상세 조회
     * - 전체 공개
     * - 나의 문의 일 때만 수정, 삭제 가능
     *
     * @param inquiryId
     * @param memberId
     * @return
     */
    @Transactional(readOnly = true)
    public InquiryResponseDto getInquiry(Long inquiryId) {
        Inquiry inquiry = findInquiry(inquiryId);

        // 문의 정보 반환
        return new InquiryResponseDto(inquiry);
    }

    /**
     * Update: 문의 수정
     * - 본인의 문의이고, 답변 대기 상태인 경우에만 수정 가능
     * - null 값이 아닌 필드만 수정
     *
     * @param inquiryId  문의 ID
     * @param memberId   사용자 ID
     * @param requestDto 수정할 문의 내용 DTO
     * @return InquiryResponseDto 수정된 문의 정보
     */
    @Transactional
    public InquiryResponseDto updateInquiry(Long inquiryId, Long memberId,
        InquiryRequestDto requestDto) {
        // 1. 해당 문의 조회
        Inquiry inquiry = findInquiry(inquiryId);

        // 2. 본인이 작성한 문의인지 확인
        validateOwnership(inquiry, memberId);

        // 3. 관리자 답변이 있는 경우 수정 불가
        if (inquiry.getInquiryState() == InquiryState.COMPLETED) {
            throw new IllegalArgumentException("답변 완료된 문의는 수정할 수 없습니다.");
        }

        // 4. 문의 업데이트
        inquiry.updateInquiry(requestDto.getInquiryTitle(), requestDto.getInquiryContent());

        // 5. 수정된 문의 정보 반환
        return new InquiryResponseDto(inquiry);
    }

    /**
     * Delete: 문의 삭제
     * - 본인 문의이고, 답변 대기 상태인 경우에만 삭제 가능
     *
     * @param inquiryId 문의 ID
     * @param memberId  사용자 ID
     */
    @Transactional
    public void deleteInquiry(Long inquiryId, Long memberId) {
        // 1. 문의 조회
        Inquiry inquiry = findInquiry(inquiryId);

        // 2. 본인이 작성한 문의인지 확인
        validateOwnership(inquiry, memberId);

        // 3. 답변이 이미 완료된 경우 삭제 불가
        if (inquiry.getInquiryState() == InquiryState.COMPLETED) {
            throw new IllegalArgumentException("답변 완료된 문의는 삭제할 수 없습니다.");
        }

        // 4. 문의 삭제
        inquiryRepository.delete(inquiry);
    }

    /**
     * 특정 ID의 회원 조회 (예외 처리 포함)
     *
     * @param memberId 사용자 ID
     * @return
     */
    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(GlobalExceptionHandler.MemberNotFoundException::new);
    }

    /**
     * 문의 조회 (예외 처리 포함)
     *
     * @param inquiryId 문의 ID
     * @return Inquiry 조회된 문의 객체
     */
    private Inquiry findInquiry(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new EntityNotFoundException("해당 문의는 존재하지 않습니다."));
    }

    /**
     * 본인 문의 여부 확인
     */
    private void validateOwnership(Inquiry inquiry, Long memberId) {
        if (!inquiry.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 문의만 수정 또는 삭제할 수 있습니다.");
        }
    }

}