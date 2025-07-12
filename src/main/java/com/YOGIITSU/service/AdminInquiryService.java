package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.AdminAnswerCreateRequestDto;
import com.YOGIITSU.dto.RequestDto.AdminAnswerUpdateRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Inquiry;
import com.YOGIITSU.entity.InquiryState;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.repository.InquiryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminInquiryService {

    private final InquiryRepository inquiryRepository;

    /**
     * 문의 답변 등록
     * - 상태가 'PROCESSING'인 문의에만 등록 가능
     * - 등록 시 상태를 'COMPLETE'로 변경
     *
     * @param inquiryId  답변할 문의 ID
     * @param requestDto 답변 제목 및 내용
     * @return InquiryResponseDto 등록 후 전체 문의 정보
     */
    @Transactional
    public InquiryResponseDto createAnswer(Long inquiryId, AdminAnswerCreateRequestDto requestDto) {

        checkAdminRole();

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new EntityNotFoundException("해당 문의가 존재하지 않습니다."));

        // 이미 답변 완료된 경우 예외 처리
        if (inquiry.getInquiryState() == InquiryState.COMPLETED) {
            throw new IllegalArgumentException("이미 답변이 등록된 문의입니다.");
        }

        inquiry.createAnswer(
            requestDto.getAnswerTitle(),
            requestDto.getAnswerContent()
        );

        return new InquiryResponseDto(inquiry);
    }

    /**
     * 문의 답변 수정
     * - 상태가 'COMPLETE'인 문의에만 수정 가능
     * - 제목이나 내용 중 하나라도 유효하면 수정
     *
     * @param inquiryId  수정할 문의 ID
     * @param requestDto  수정할 제목 및 내용
     * @return InquiryResponseDto 수정 후 전체 문의 정보
     */
    @Transactional
    public InquiryResponseDto updateAnswer(Long inquiryId, AdminAnswerUpdateRequestDto requestDto) {

        checkAdminRole();

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new EntityNotFoundException("해당 문의가 존재하지 않습니다."));

        // 답변이 등록되지 않은 경우 수정 불가
        if (inquiry.getInquiryState() != InquiryState.COMPLETED) {
            throw new IllegalArgumentException("아직 답변이 등록되지 않아 수정할 수 없습니다.");
        }

        String title = requestDto.getAnswerTitle();
        String content = requestDto.getAnswerContent();

        if ((title == null || title.isBlank()) && (content == null || content.isBlank())) {
            throw new IllegalArgumentException("수정할 답변 제목 또는 내용을 입력하세요.");
        }

        inquiry.updateAnswer(title, content);
        return new InquiryResponseDto(inquiry);
    }

    /**
     * 문의글 삭제
     * - 관리자 권한으로 문의글과 그에 대한 답변 전체 삭제
     * - 상태와 무관하게 강제 삭제 가능
     *
     * @param inquiryId 삭제할 문의 ID
     */
    @Transactional
    public void deleteInquiryByAdmin(Long inquiryId) {

        checkAdminRole();

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new EntityNotFoundException("해당 문의가 존재하지 않습니다."));

        inquiryRepository.delete(inquiry);
    }

    private void checkAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new SecurityException("잘못된 사용자 정보입니다.");
        }

        if (!"ADMIN".equals(userDetails.getRole())) {
            throw new SecurityException("관리자만 접근할 수 있습니다.");
        }

    }
}
