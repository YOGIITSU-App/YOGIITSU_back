package com.YOGIITSU.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
import com.YOGIITSU.dto.RequestDto.AdminAnswerCreateRequestDto;
import com.YOGIITSU.dto.RequestDto.AdminAnswerUpdateRequestDto;
import com.YOGIITSU.dto.ResponseDto.InquiryResponseDto;
import com.YOGIITSU.enums.InquiryState;
import com.YOGIITSU.exception.resource.InquiryNotFoundException;
import com.YOGIITSU.exception.validation.InvalidInquiryStateException;
import com.YOGIITSU.exception.validation.MissingRequiredFieldException;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.service.AdminInquiryService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminInquiryController.class)
@Import(GlobalExceptionHandler.class)
public class AdminInquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminInquiryService adminInquiryService;

    /* =============== helpers: auth stubbing =============== */
    private void stubAdminAuth() {
        // CustomUserDetails 생성
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN"));
        CustomUserDetails principal = new CustomUserDetails(
            1L, "adminMaster", "관리자", "admin@yogiitsu.com", "pass123", "ADMIN", authorities
        );

        // SecurityContextHolder에 주입
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /* ================= CREATE: 관리자 답변 등록 ================= */
    @DisplayName("답변등록_성공")
    @Test
    void createAnswer_success() throws Exception {
        Long inquiryId = 1L;
        stubAdminAuth();
        InquiryResponseDto responseDto = createDummyResponseDto(inquiryId, InquiryState.COMPLETED);

        when(
            adminInquiryService.createAnswer(eq(inquiryId), any(AdminAnswerCreateRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answerTitle": "답변 제목입니다.",
                        "answerContent": "답변 내용입니다."
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.inquiryState").value("COMPLETED"))
            .andExpect(jsonPath("$.answerTitle").value("답변 제목입니다."));
    }

    @DisplayName("답변등록_실패_유효성검증")
    @Test
    void createAnswer_fail_validation() throws Exception {
        Long inquiryId = 1L;
        stubAdminAuth();

        // 제목과 내용이 비어있는 요청
        mockMvc.perform(post("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answerTitle": "",
                        "answerContent": ""
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @DisplayName("답변등록_실패_답변완료")
    @Test
    void createAnswer_fail_alreadyCompleted() throws Exception {
        Long inquiryId = 1L;
        stubAdminAuth();

        when(
            adminInquiryService.createAnswer(eq(inquiryId), any(AdminAnswerCreateRequestDto.class)))
            .thenThrow(new InvalidInquiryStateException("이미 답변이 등록된 문의입니다."));

        mockMvc.perform(post("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answerTitle": "제목",
                        "answerContent": "내용"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @DisplayName("답변등록_실패_존재하지않음")
    @Test
    void createAnswer_fail_notFound() throws Exception {
        Long inquiryId = 999L;
        stubAdminAuth();

        when(
            adminInquiryService.createAnswer(eq(inquiryId), any(AdminAnswerCreateRequestDto.class)))
            .thenThrow(new InquiryNotFoundException(inquiryId));

        mockMvc.perform(post("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answerTitle": "제목",
                        "answerContent": "내용"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    /* ================= UPDATE: 관리자 답변 수정 ================= */
    @DisplayName("답변수정_성공")
    @Test
    void updateAnswer_success() throws Exception {
        Long inquiryId = 1L;
        stubAdminAuth();

        InquiryResponseDto responseDto = createDummyResponseDto(inquiryId, InquiryState.COMPLETED);
        ReflectionTestUtils.setField(responseDto, "answerTitle", "수정된 제목");
        ReflectionTestUtils.setField(responseDto, "answerContent", "수정된 내용");

        when(
            adminInquiryService.updateAnswer(eq(inquiryId), any(AdminAnswerUpdateRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(put("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answerTitle": "수정된 제목",
                        "answerContent": "수정된 내용"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.answerTitle").value("수정된 제목"));
    }

    @DisplayName("답변수정_실패_문의없음")
    @Test
    void updateAnswer_fail_notFound() throws Exception {
        Long inquiryId = 99L;
        stubAdminAuth();

        when(
            adminInquiryService.updateAnswer(eq(inquiryId), any(AdminAnswerUpdateRequestDto.class)))
            .thenThrow(new InquiryNotFoundException(inquiryId));

        mockMvc.perform(put("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answerTitle": "제목",
                        "answerContent": "내용"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @DisplayName("답변수정_실패_변경사항없음")
    @Test
    void updateAnswer_fail_noChanges() throws Exception {
        Long inquiryId = 1L;
        stubAdminAuth();

        when(
            adminInquiryService.updateAnswer(eq(inquiryId), any(AdminAnswerUpdateRequestDto.class)))
            .thenThrow(new MissingRequiredFieldException("수정할 제목 또는 내용을 입력하세요."));

        mockMvc.perform(put("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answerTitle": null,
                        "answerContent": ""
                    }
                    """))
            .andExpect(status().isBadRequest()); // 400 Expect
    }

    @DisplayName("답변수정_실패_필수값누락")
    @Test
    void updateAnswer_fail_missingFields() throws Exception {
        Long inquiryId = 1L;
        stubAdminAuth();

        when(
            adminInquiryService.updateAnswer(eq(inquiryId), any(AdminAnswerUpdateRequestDto.class)))
            .thenThrow(new MissingRequiredFieldException("수정할 내용이 없습니다."));

        mockMvc.perform(put("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "answerTitle": null,
                        "answerContent": null
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    /* ================= DELETE: 관리자 문의 삭제 ================= */
    @DisplayName("문의글강제삭제_성공_대기상태")
    @Test
    void deleteInquiryByAdmin_success_processing() throws Exception {
        Long inquiryId = 1L;
        stubAdminAuth();

        doNothing().when(adminInquiryService).deleteInquiryByAdmin(inquiryId);

        mockMvc.perform(delete("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf()))
            .andExpect(status().isNoContent()); // 204 Expect
    }

    @DisplayName("문의글강제삭제_성공_답변완료")
    @Test
    void deleteInquiryByAdmin_success_completed() throws Exception {
        Long inquiryId = 2L;
        stubAdminAuth();

        doNothing().when(adminInquiryService).deleteInquiryByAdmin(inquiryId);

        mockMvc.perform(delete("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf()))
            .andExpect(status().isNoContent()); // 204 Expect
    }

    @DisplayName("문의글강제삭제_실패_존재하지않음")
    @Test
    void deleteInquiryByAdmin_fail_notFound() throws Exception {
        Long inquiryId = 99L;
        stubAdminAuth();

        doThrow(new InquiryNotFoundException(inquiryId))
            .when(adminInquiryService).deleteInquiryByAdmin(inquiryId);

        mockMvc.perform(delete("/admin/inquiries/{inquiryId}", inquiryId)
                .with(csrf()))
            .andExpect(status().isNotFound());
    }


    /* ================= Helper Methods ================= */
    private InquiryResponseDto createDummyResponseDto(Long id, InquiryState state) {
        return new InquiryResponseDto(
            id,
            "문의 제목",
            "문의 내용",
            LocalDateTime.now(),
            state,
            "작성자",
            "답변 제목입니다.",
            "답변 내용입니다.",
            LocalDateTime.now(),
            false
        );
    }
}
