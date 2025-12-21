package com.YOGIITSU.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.YOGIITSU.dto.RequestDto.AdminAnswerCreateRequestDto;
import com.YOGIITSU.dto.RequestDto.AdminAnswerUpdateRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.entity.Inquiry;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.enums.InquiryState;
import com.YOGIITSU.exception.resource.InquiryNotFoundException;
import com.YOGIITSU.exception.validation.InvalidInquiryStateException;
import com.YOGIITSU.exception.validation.MissingRequiredFieldException;
import com.YOGIITSU.repository.InquiryRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class AdminInquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private AdminInquiryService adminInquiryService;

    /* ================= CREATE: 문의 답변 등록 ================= */
    @DisplayName("답변등록_성공_답변대기상태")
    @Test
    void createAnswer_success_processing() {
        Long inquiryId = 1L;
        Inquiry processingInquiry = createDummyInquiry(inquiryId, InquiryState.PROCESSING);
        AdminAnswerCreateRequestDto dto = new AdminAnswerCreateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", "답변 제목");
        ReflectionTestUtils.setField(dto, "answerContent", "답변 내용");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(processingInquiry));

        InquiryResponseDto result = adminInquiryService.createAnswer(inquiryId, dto);

        assertEquals(InquiryState.COMPLETED, processingInquiry.getInquiryState());
        assertEquals("답변 제목", processingInquiry.getAnswerTitle());
        assertEquals("답변 내용", processingInquiry.getAnswerContent());
        assertNotNull(result);
    }

    @DisplayName("답변등록_실패_답변완료상태")
    @Test
    void createAnswer_fail_Completed() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createDummyInquiry(inquiryId, InquiryState.COMPLETED);

        AdminAnswerCreateRequestDto dto = new AdminAnswerCreateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", "답변 제목");
        ReflectionTestUtils.setField(dto, "answerContent", "답변 내용");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));

        assertThrows(InvalidInquiryStateException.class, () ->
            adminInquiryService.createAnswer(inquiryId, dto));
    }

    @DisplayName("답변등록_실패_존재하지않음")
    @Test
    void createAnswer_fail_notFound() {
        Long inquiryId = 99L;

        AdminAnswerCreateRequestDto dto = new AdminAnswerCreateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", "답변 제목");
        ReflectionTestUtils.setField(dto, "answerContent", "답변 내용");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.empty());

        assertThrows(InquiryNotFoundException.class, () ->
            adminInquiryService.createAnswer(inquiryId, dto));
    }

    /* ================= UPDATE: 문의 답변 수정 ================= */
    @DisplayName("답변수정_성공_제목민")
    @Test
    void uodateAnswer_success_titleOnly() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createCompletedInquiry(inquiryId);

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", "새 답변 제목");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));

        adminInquiryService.updateAnswer(inquiryId, dto);

        assertEquals("새 답변 제목", completedInquiry.getAnswerTitle());
        assertEquals("기존 답변 내용", completedInquiry.getAnswerContent());
    }

    @DisplayName("답변수정_성공_내용만")
    @Test
    void updateAnswer_success_contentOnly() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createCompletedInquiry(inquiryId);

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerContent", "새 답변 내용");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));

        adminInquiryService.updateAnswer(inquiryId, dto);

        assertEquals("기존 답변 제목", completedInquiry.getAnswerTitle());
        assertEquals("새 답변 내용", completedInquiry.getAnswerContent());
    }

    @DisplayName("답변수정_성공_제목과내용")
    @Test
    void updateAnswer_success_bothFields() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createCompletedInquiry(inquiryId);

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", "수정된 답변 제목");
        ReflectionTestUtils.setField(dto, "answerContent", "수정된 답변 내용");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));

        adminInquiryService.updateAnswer(inquiryId, dto);

        assertEquals("수정된 답변 제목", completedInquiry.getAnswerTitle());
        assertEquals("수정된 답변 내용", completedInquiry.getAnswerContent());
    }

    @DisplayName("답변수정_실패_답변대기상태")
    @Test
    void updateAnswer_fail_processing() {
        Long inquiryId = 1L;
        Inquiry processingInquiry = createDummyInquiry(inquiryId, InquiryState.PROCESSING);

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", "새 답변 제목");
        ReflectionTestUtils.setField(dto, "answerContent", "새 답변 내용");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(processingInquiry));

        assertThrows(InvalidInquiryStateException.class, () ->
            adminInquiryService.updateAnswer(inquiryId, dto));
    }

    @DisplayName("답변수정_실패_둘다null")
    @Test
    void updateAnswer_fail_bothNull() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createCompletedInquiry(inquiryId);

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", null);
        ReflectionTestUtils.setField(dto, "answerContent", null);

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));

        assertThrows(MissingRequiredFieldException.class, () ->
            adminInquiryService.updateAnswer(inquiryId, dto));
    }

    @DisplayName("답변수정_실패_둘다Blank")
    @Test
    void updateAnswer_fail_bothBlank() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createCompletedInquiry(inquiryId);

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", "   "); // 공백
        ReflectionTestUtils.setField(dto, "answerContent", "");  // 빈 문자열

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));

        assertThrows(MissingRequiredFieldException.class, () ->
            adminInquiryService.updateAnswer(inquiryId, dto));
    }

    @DisplayName("답변수정_실패_변경사항없음_혼합")
    @Test
    void updateAnswer_fail_noChanges_mixed() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createCompletedInquiry(inquiryId);

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", null);
        ReflectionTestUtils.setField(dto, "answerContent", "   ");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));

        assertThrows(MissingRequiredFieldException.class, () ->
            adminInquiryService.updateAnswer(inquiryId, dto));
    }

    @DisplayName("답변수정_실패_변경사항없음")
    @Test
    void updateAnswer_fail_noChanges() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createCompletedInquiry(inquiryId);

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", null);
        ReflectionTestUtils.setField(dto, "answerContent", " ");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));

        assertThrows(MissingRequiredFieldException.class, () ->
            adminInquiryService.updateAnswer(inquiryId, dto));
    }

    @DisplayName("답변수정_실패_문의없음")
    @Test
    void updateAnswer_fail_inquiryNotFound() {
        Long inquiryId = 99L;

        AdminAnswerUpdateRequestDto dto = new AdminAnswerUpdateRequestDto();
        ReflectionTestUtils.setField(dto, "answerTitle", "새 답변 제목");

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.empty());

        assertThrows(InquiryNotFoundException.class, () ->
            adminInquiryService.updateAnswer(inquiryId, dto));
    }

    /* ================= DELETE: 문의글 강제 삭제 (관리자) ================= */
    @DisplayName("문의글강제삭제_성공_답변완료")
    @Test
    void deleteInquiryByAdmin_success_completed() {
        Long inquiryId = 1L;
        Inquiry completedInquiry = createCompletedInquiry(inquiryId);

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(completedInquiry));
        doNothing().when(inquiryRepository).delete(completedInquiry);

        adminInquiryService.deleteInquiryByAdmin(inquiryId);

        verify(inquiryRepository).delete(completedInquiry);
    }

    @DisplayName("문의글강제삭제_성공_답변대기")
    @Test
    void deleteInquiryByAdmin_success_processing() {
        Long inquiryId = 1L;
        Inquiry processingInquiry = createDummyInquiry(inquiryId, InquiryState.PROCESSING);

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.of(processingInquiry));
        doNothing().when(inquiryRepository).delete(processingInquiry);

        adminInquiryService.deleteInquiryByAdmin(inquiryId);

        verify(inquiryRepository).delete(processingInquiry);
    }

    @DisplayName("문의글강제삭제_실패_문의없음")
    @Test
    void deleteInquiryByAdmin_fail_inquiryNotFound() {
        Long inquiryId = 99L;

        when(inquiryRepository.findById(inquiryId)).thenReturn(Optional.empty());

        assertThrows(InquiryNotFoundException.class, () ->
            adminInquiryService.deleteInquiryByAdmin(inquiryId));

        verify(inquiryRepository, never()).delete(any());
    }

    /* ================= Dummy methods ================= */
    private Member createDummyMember(Long memberId) {
        return Member.builder().id(memberId).userName("테스트회원").build();
    }

    // 기본 문의 생성 (PROCESSING 상태)
    private Inquiry createDummyInquiry(Long inquiryId, InquiryState state) {
        return Inquiry.builder()
            .inquiryId(inquiryId)
            .member(createDummyMember(1L))
            .inquiryTitle("사용자 문의 제목")
            .inquiryContent("사용자 문의 내용")
            .inquiryState(state)
            .build();
    }

    // 답변 완료된 문의 생성 (UPDATE 테스트용)
    private Inquiry createCompletedInquiry(Long inquiryId) {
        // 실제 Inquiry 엔티티는 createAnswer 등의 메서드를 통해 상태와 답변 필드가 설정된다고 가정
        Inquiry inquiry = createDummyInquiry(inquiryId, InquiryState.COMPLETED);

        // 필드를 직접 설정하여 답변 완료 상태를 Mocking
        try {
            java.lang.reflect.Field stateField = Inquiry.class.getDeclaredField("inquiryState");
            stateField.setAccessible(true);
            stateField.set(inquiry, InquiryState.COMPLETED);

            java.lang.reflect.Field answerTitleField = Inquiry.class.getDeclaredField("answerTitle");
            answerTitleField.setAccessible(true);
            answerTitleField.set(inquiry, "기존 답변 제목");

            java.lang.reflect.Field answerContentField = Inquiry.class.getDeclaredField("answerContent");
            answerContentField.setAccessible(true);
            answerContentField.set(inquiry, "기존 답변 내용");
        } catch (Exception e) {
            throw new RuntimeException("Reflection failed during test setup", e);
        }

        return inquiry;
    }
}
