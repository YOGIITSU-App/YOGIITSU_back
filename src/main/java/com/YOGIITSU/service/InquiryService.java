package com.YOGIITSU.service;

import com.YOGIITSU.exception.auth.AccessDeniedException;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.dto.RequestDto.InquiryRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryListResponseDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Inquiry;
import com.YOGIITSU.enums.InquiryState;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.validation.InvalidInquiryStateException;
import com.YOGIITSU.exception.validation.MissingRequiredFieldException;
import com.YOGIITSU.repository.InquiryRepository;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.exception.resource.InquiryNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * InquiryService
 * - 문의 등록, 조회, 수정, 삭제 비즈니스 로직 처리
 * - 비회원은 전체/단건 조회만 가능
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

	private final InquiryRepository inquiryRepository;
	private final MemberRepository memberRepository;

	/**
	 * Create: 문의 등록
	 * - 상태를 '답변 대기'로 설정한 후 저장
	 * - 제목/내용 유효성 검증은 InquiryRequestDto(@NotBlank)에서 처리
	 *
	 * @param requestDto 문의 요청 데이터
	 * @param memberId   문의 작성한 회원 ID
	 */
	@Transactional
	public void createInquiry(InquiryRequestDto requestDto, Long memberId) {

		// 1. 비회원 접근 차단
		if (memberId == null) {
			throw new AccessDeniedException("로그인이 필요한 기능입니다.");
		}

		// 2. 회원 조회
		Member member = findMember(memberId);

		// 3. 요청 DTO와 회원 정보를 기반으로 Inquiry 엔티티 생성
		Inquiry inquiry = Inquiry.builder()
			.member(member)
			.inquiryTitle(requestDto.getInquiryTitle())
			.inquiryContent(requestDto.getInquiryContent())
			.inquiryState(InquiryState.PROCESSING)  // 기본 상태: PROCESSING(답변대기)
			.build();

		// 4. 생성된 문의 DB에 저장
		inquiryRepository.save(inquiry);
		log.info("[UserInquiry] 문의 등록 완료 - inquiryId={}", inquiry.getInquiryId());
	}

	/**
	 * Read-list: 전체 문의 리스트 조회 (비회원 가능)
	 * - 최신순으로 정렬 - 제목, 상태, 작성자, 작성일만 포함된 DTO 리스트 반환
	 * - memberId가 null이면 비회원으로 간주
	 *
	 * @return InquiryListResponseDto 문의 리스트
	 */
	@Transactional(readOnly = true)
	public List<InquiryListResponseDto> getAllInquiries(Long memberId) {
		List<Inquiry> inquiries = inquiryRepository.findAllByOrderByInquiryAtDesc();

		return inquiries.stream().map(inquiry -> {
			boolean isMine = (memberId != null && inquiry.getMember().getId().equals(memberId));
			return new InquiryListResponseDto(inquiry, isMine);
		}).collect(Collectors.toList());
	}

	/**
	 * Read: 문의 상세 조회
	 * - 전체 공개
	 * - 나의 문의 일 때만 수정, 삭제 가능
	 *
	 * @param inquiryId 문의 ID
	 * @return InquiryResponseDto 문의 상세 정보
	 */
	@Transactional(readOnly = true)
	public InquiryResponseDto getInquiry(Long inquiryId, Long memberId) {
		Inquiry inquiry = findInquiry(inquiryId);

		boolean isMine = (memberId != null && inquiry.getMember().getId().equals(memberId));
		// 문의 정보 반환
		return new InquiryResponseDto(inquiry, isMine);
	}

	/**
	 * Update: 문의 수정
	 * - 본인의 문의이고, 답변 대기 상태인 경우에만 수정 가능
	 * - 제목/내용 중 null 값은 무시하고 기존 값 유지
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
			throw new InvalidInquiryStateException("답변 완료된 문의는 수정할 수 없습니다.");
		}

		String title = requestDto.getInquiryTitle();
		String content = requestDto.getInquiryContent();

		// 4. 제목과 내용이 모두 비어 있으면 예외 발생
		if ((title == null || title.isBlank()) && (content == null || content.isBlank())) {
			throw new MissingRequiredFieldException("수정할 제목 또는 내용을 입력하세요.");
		}

		// 5. 유효한 항목만 수정 (엔티티 내부에서 처리)
		inquiry.updateInquiry(title, content);
		log.info("[UserInquiry] 문의 수정 완료 - inquiryId={}", inquiryId);

		// 6. 수정된 문의 정보 반환
		return new InquiryResponseDto(inquiry, true);
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
			throw new InvalidInquiryStateException("답변 완료된 문의는 삭제할 수 없습니다.");
		}

		// 4. 문의 삭제
		inquiryRepository.delete(inquiry);
		log.info("[UserInquiry] 문의 삭제 완료 - inquiryId={}", inquiryId);
	}

    /*
      ===== 공통 유틸 메서드 =====
     */

	/**
	 * 특정 ID의 회원 조회 (예외 처리 포함)
	 */
	private Member findMember(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(MemberNotFoundException::new);
	}

	/**
	 * 문의 조회 (예외 처리 포함)
	 */
	private Inquiry findInquiry(Long inquiryId) {
		return inquiryRepository.findById(inquiryId)
			.orElseThrow(InquiryNotFoundException::new);
	}

	/**
	 * 본인 문의 여부 확인
	 */
	private void validateOwnership(Inquiry inquiry, Long memberId) {
		if (!inquiry.getMember().getId().equals(memberId)) {
			throw new AccessDeniedException("본인이 작성한 문의만 수정 또는 삭제할 수 있습니다.");
		}
	}
}
