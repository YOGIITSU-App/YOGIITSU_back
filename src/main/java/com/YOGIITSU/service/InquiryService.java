package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
import com.YOGIITSU.dto.RequestDto.InquiryPasswordDto;
import com.YOGIITSU.dto.RequestDto.InquiryRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryListResponseDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Inquiry;
import com.YOGIITSU.entity.InquiryState;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.InquiryRepository;
import com.YOGIITSU.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create: 문의 등록
     *
     * @param requestDto 문의 요청 데이터
     * @param memberId   문의 작성한 회원 ID
     * @return InquiryResponseDto 등록된 문의 정보
     */
    @Transactional
    public InquiryResponseDto createInquiry(InquiryRequestDto requestDto, Long memberId) {

        // 1. 제목과 내용이 비어 있는지 확인. 비어 있으면 안내 메시지
        if (requestDto.getInquiryTitle() == null || requestDto.getInquiryTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("문의 제목을 입력해주세요.");
        }
        if (requestDto.getInquiryContent() == null || requestDto.getInquiryContent().trim().isEmpty()) {
            throw new IllegalArgumentException("문의 내용을 입력해주세요.");
        }

        // 2. 회원 조회
        Member member = findMember(memberId);

        Inquiry inquiry = Inquiry.builder()
            .member(member)
            .inquiryTitle(requestDto.getInquiryTitle())
            .inquiryContent(requestDto.getInquiryContent())
            .inquiryState(InquiryState.PROCESSING)  // 기본 상태: PROCESSING(답변대기)
            .inquiryPassword(requestDto.getInquiryPassword())
            .build();

        // 5. 생성된 문의 DB에 저장 & 반환
        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return new InquiryResponseDto(savedInquiry);
    }

    /**
     * Read-list: 모든 문의 내역 리스트 조회
     * - 비밀번호 없이 조회 가능
     *
     * @return InquiryListResponseDto 문의 리스트
     */
    @Transactional(readOnly = true)
    public List<InquiryListResponseDto> getAllInquiries() {
        return inquiryRepository.findAll().stream()
            .map(InquiryListResponseDto::new)
            .collect(Collectors.toList());
    }

    /**
     * Read-my: 개인 문의 조회
     * - 비밀번호가 일치해야 조회 가능
     *
     * @param inquiryId  문의 ID
     * @param memberId  사용자 ID
     * @return InquiryResponseDto 개인 문의 정보
     */
    @Transactional(readOnly = true)
    public InquiryResponseDto getInquiry(Long inquiryId, Long memberId, InquiryPasswordDto requestDto) {
        Inquiry inquiry = findInquiry(inquiryId);
        validateOwnership(inquiry, memberId);

        // 2. 비밀번호 검증
        if (!inquiry.getInquiryPassword().equals(requestDto.getInquiryPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        // 3. 문의 정보 반환
        return new InquiryResponseDto(inquiry);
    }

    /**
     * Update: 문의 수정
     * - 관리자 답변 후 수정 불가
     *
     * @param inquiryId  문의 ID
     * @param memberId  사용자 ID
     * @param requestDto  수정할 문의 내용 DTO
     * @return InquiryResponseDto 수정된 문의 정보
     */
    @Transactional
    public InquiryResponseDto updateInquiry(Long inquiryId, Long memberId,
        InquiryRequestDto requestDto) {
        // 1. 해당 문의 조회
        Inquiry inquiry = findInquiry(inquiryId);

        // 2. 본인이 작성한 문의인지 확인 (비밀번호 검증은 단건 조회에서 완료)
        validateOwnership(inquiry, memberId);

        // 3. 관리자 답변이 있는 경우 수정 불가
        if (inquiry.getResponse() != null) {
            throw new IllegalArgumentException("답변 완료된 문의는 수정할 수 없습니다.");
        }

        // 4. 문의 업데이트
        String updateTitle = (requestDto.getInquiryTitle() != null) ? requestDto.getInquiryTitle()
            : inquiry.getInquiryTitle();
        String updateContent = (requestDto.getInquiryContent() != null) ? requestDto.getInquiryContent()
            : inquiry.getInquiryContent();

        inquiry.updateInquiry(updateTitle, updateContent);

        // 5. 수정된 문의 정보 반환
        return new InquiryResponseDto(inquiry);
    }

    /**
     * Delete: 문의 삭제
     *
     * @param inquiryId  문의 ID
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
     * 특정 ID의 회원 조회
     *
     * @param memberId  사용자 ID
     * @return
     */
    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(GlobalExceptionHandler.MemberNotFoundException::new);
    }

    /**
     * 특정 ID의 문의 조회
     *
     * @param inquiryId  문의 ID
     * @return
     */
    private Inquiry findInquiry(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new IllegalArgumentException("해당 문의는 존재하지 않습니다."));
    }

    /**
     * 본인 문의 여부 확인
     */
    private void validateOwnership(Inquiry inquiry, Long memberId) {
        if (!inquiry.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인이 작성한 문의만 조회할 수 있습니다.");
        }
    }

}