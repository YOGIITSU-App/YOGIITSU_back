package com.YOGIITSU.service;

import static org.mockito.ArgumentMatchers.any;

import com.YOGIITSU.dto.RequestDto.InquiryRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryListResponseDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Inquiry;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.enums.InquiryState;
import com.YOGIITSU.exception.auth.AccessDeniedException;
import com.YOGIITSU.exception.resource.InquiryNotFoundException;
import com.YOGIITSU.exception.validation.InvalidInquiryStateException;
import com.YOGIITSU.exception.validation.MissingRequiredFieldException;
import com.YOGIITSU.repository.InquiryRepository;
import com.YOGIITSU.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private InquiryService inquiryService;

    /* ================= CREATE: 문의 등록 ================= */

    @DisplayName("문의등록_성공")
    @Test
    void createInquiry_success() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        InquiryRequestDto dto = new InquiryRequestDto("제목", "내용");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        inquiryService.createInquiry(dto, memberId);

        verify(memberRepository).findById(memberId);
        verify(inquiryRepository).save(any(Inquiry.class));
    }

    @DisplayName("문의등록_실패_비회원")
    @Test
    void createInquiry_fail_guest() {
        InquiryRequestDto dto = new InquiryRequestDto("제목", "내용");

        assertThrows(AccessDeniedException.class, () ->
            inquiryService.createInquiry(dto, null));

        verify(inquiryRepository, never()).save(any());
    }

    /* ================= READ-LIST: 문의 전체 리스트 조회 ================= */

    @DisplayName("문의리스트조회_성공_비회원")
    @Test
    void getAllInquiries_success_guest() {
        Member member = createDummyMember(1L);
        Inquiry inquiry = createDummyInquiry(member);

        when(inquiryRepository.findAllByOrderByInquiryAtDesc()).thenReturn(List.of(inquiry));

        List<InquiryListResponseDto> result = inquiryService.getAllInquiries(null);

        assertEquals(1, result.size());
        assertFalse(result.getFirst().isMine());
        verify(inquiryRepository).findAllByOrderByInquiryAtDesc();
    }

    @DisplayName("문의리스트조회_성공_회원")
    @Test
    void getAllInquiries_success_member() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createDummyInquiry(member);

        when(inquiryRepository.findAllByOrderByInquiryAtDesc()).thenReturn(List.of(inquiry));

        List<InquiryListResponseDto> result = inquiryService.getAllInquiries(memberId);

        assertEquals(1, result.size());
        assertTrue(result.getFirst().isMine());
    }

    /* ================= READ-DETAIL: 문의 상세 조회 ================= */

    @DisplayName("문의상세조회_성공_내문의")
    @Test
    void getInquiry_success_mine() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createDummyInquiry(member);

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        InquiryResponseDto result = inquiryService.getInquiry(1L, memberId);

        assertNotNull(result);
        assertTrue(result.isMine());
    }

    @DisplayName("문의상세조회_성공_내문의아님")
    @Test
    void getInquiry_success_notMine() {
        Member member = createDummyMember(1L);
        Inquiry inquiry = createDummyInquiry(member);

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        InquiryResponseDto result = inquiryService.getInquiry(1L, 2L);

        assertNotNull(result);
        assertFalse(result.isMine());
    }

    @DisplayName("문의상세조회_성공_비회원")
    @Test
    void getInquiry_success_guest() {
        Member member = createDummyMember(1L);
        Inquiry inquiry = createDummyInquiry(member);

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        InquiryResponseDto result = inquiryService.getInquiry(1L, null);

        assertNotNull(result);
        assertFalse(result.isMine());
    }

    @DisplayName("문의상세조회_실패_존재하지않음")
    @Test
    void getInquiry_fail_notFound() {
        when(inquiryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InquiryNotFoundException.class, () ->
            inquiryService.getInquiry(1L, 1L));
    }

    /* ================= UPDATE: 문의 수정 ================= */

    @DisplayName("문의수정_실패_내문의아님")
    @Test
    void updateInquiry_fail_notOwner() {
        Member member = createDummyMember(1L);
        Inquiry inquiry = createDummyInquiry(member);
        InquiryRequestDto dto = new InquiryRequestDto("수정제목", "수정내용");

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThrows(AccessDeniedException.class, () ->
            inquiryService.updateInquiry(1L, 2L, dto));
    }

    @DisplayName("문의수정_성공_제목만")
    @Test
    void updateInquiry_success_titleOnly() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createDummyInquiry(member);
        InquiryRequestDto dto = new InquiryRequestDto("새 제목", null);

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        InquiryResponseDto result = inquiryService.updateInquiry(1L, memberId, dto);

        assertEquals("새 제목", inquiry.getInquiryTitle());
        assertNotNull(result);
    }

    @DisplayName("문의수정_성공_내용만")
    @Test
    void updateInquiry_success_contentOnly() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createDummyInquiry(member);
        InquiryRequestDto dto = new InquiryRequestDto(null, "새 내용");

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        InquiryResponseDto result = inquiryService.updateInquiry(1L, memberId, dto);

        assertEquals("새 내용", inquiry.getInquiryContent());
        assertNotNull(result);
    }

    @DisplayName("문의수정_성공_제목과내용")
    @Test
    void updateInquiry_success_bothFields() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createDummyInquiry(member);
        InquiryRequestDto dto = new InquiryRequestDto("새 제목", "새 내용");

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        InquiryResponseDto result = inquiryService.updateInquiry(1L, memberId, dto);

        assertEquals("새 제목", inquiry.getInquiryTitle());
        assertEquals("새 내용", inquiry.getInquiryContent());
        assertNotNull(result);
    }

    @DisplayName("문의수정_실패_변경사항없음")
    @Test
    void updateInquiry_fail_noChanges() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createDummyInquiry(member);
        String originalTitle = inquiry.getInquiryTitle();
        String originalContent = inquiry.getInquiryContent();

        InquiryRequestDto dto = new InquiryRequestDto(null, null);
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThrows(MissingRequiredFieldException.class, () ->
            inquiryService.updateInquiry(1L, memberId, dto));

        assertEquals(originalTitle, inquiry.getInquiryTitle());
        assertEquals(originalContent, inquiry.getInquiryContent());
    }

    @DisplayName("문의수정_실패_답변완료")
    @Test
    void updateInquiry_fail_completed() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createCompletedInquiry(member);

        InquiryRequestDto dto = new InquiryRequestDto("새 제목", "새 내용");
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThrows(InvalidInquiryStateException.class, () ->
            inquiryService.updateInquiry(1L, memberId, dto));
    }

    @DisplayName("문의수정_실패_제목내용모두빈값")
    @Test
    void updateInquiry_fail_emptyFields() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createDummyInquiry(member);
        InquiryRequestDto dto = new InquiryRequestDto(" ", " ");

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThrows(MissingRequiredFieldException.class, () ->
            inquiryService.updateInquiry(1L, memberId, dto));
    }

    /* ================= DELETE ================= */

    @DisplayName("문의삭제_성공")
    @Test
    void deleteInquiry_success() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createDummyInquiry(member);

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        inquiryService.deleteInquiry(1L, memberId);
        verify(inquiryRepository).delete(inquiry);
    }

    @DisplayName("문의삭제_실패_내문의아님")
    @Test
    void deleteInquiry_fail_notOwner() {
        Member member = createDummyMember(1L);
        Inquiry inquiry = createDummyInquiry(member);

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThrows(AccessDeniedException.class, () ->
            inquiryService.deleteInquiry(1L, 2L));
    }

    @DisplayName("문의삭제_실패_답변완료")
    @Test
    void deleteInquiry_fail_completed() {
        Long memberId = 1L;
        Member member = createDummyMember(memberId);
        Inquiry inquiry = createCompletedInquiry(member);

        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(inquiry));

        assertThrows(InvalidInquiryStateException.class, () ->
            inquiryService.deleteInquiry(1L, memberId));
    }

    /* ================= Dummy methods ================= */

    private Member createDummyMember(Long memberId) {
        return Member.builder()
            .id(memberId)
            .userName("테스트회원")
            .build();
    }

    private Inquiry createDummyInquiry(Member member) {
        return Inquiry.builder()
            .inquiryId(1L)
            .member(member)
            .inquiryTitle("테스트제목")
            .inquiryContent("테스트내용")
            .inquiryState(InquiryState.PROCESSING)
            .build();
    }

    private Inquiry createCompletedInquiry(Member member) {
        Inquiry inquiry = createDummyInquiry(member);

        // 리플렉션을 통해 상태를 강제로 COMPLETED로 변경
        try {
            java.lang.reflect.Field stateField = Inquiry.class.getDeclaredField("inquiryState");
            stateField.setAccessible(true);
            stateField.set(inquiry, InquiryState.COMPLETED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return inquiry;
    }
}
