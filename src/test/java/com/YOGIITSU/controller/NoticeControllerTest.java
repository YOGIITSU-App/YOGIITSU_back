package com.YOGIITSU.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
import com.YOGIITSU.dto.RequestDto.NoticeRequestDto;
import com.YOGIITSU.dto.ResponseDto.NoticeDetailResponseDto;
import com.YOGIITSU.dto.ResponseDto.NoticeListResponseDto;
import com.YOGIITSU.exception.resource.NoticeNotFoundException;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.service.NoticeService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NoticeController.class)
@Import(GlobalExceptionHandler.class)
public class NoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeService noticeService;

    /* =============== helpers =============== */

    private void stubAdminAuth() {
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_ADMIN"));
        CustomUserDetails principal = new CustomUserDetails(
            1L, "adminMaster", "관리자", "admin@yogiitsu.com", "pass123", "ADMIN", authorities
        );
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private NoticeListResponseDto noticeListDto(Long id, String title, LocalDateTime at) {
        return new NoticeListResponseDto(id, title, at);
    }

    private NoticeDetailResponseDto noticeDetailDto(Long id, String title, String content, LocalDateTime at) {
        return new NoticeDetailResponseDto(id, title, content, at);
    }

    /* =============== GET /notices =============== */

    @Test
    @WithMockUser
    @DisplayName("공지사항_전체조회_성공")
    void getNotices_success() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<NoticeListResponseDto> notices = List.of(
            noticeListDto(1L, "공지사항 1", now),
            noticeListDto(2L, "공지사항 2", now.minusHours(1))
        );

        when(noticeService.getAllNotices()).thenReturn(notices);

        mockMvc.perform(get("/notices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].noticeId").value(1))
            .andExpect(jsonPath("$[0].noticeTitle").value("공지사항 1"))
            .andExpect(jsonPath("$[1].noticeId").value(2))
            .andExpect(jsonPath("$[1].noticeTitle").value("공지사항 2"));

        verify(noticeService).getAllNotices();
    }

    @Test
    @WithMockUser
    @DisplayName("공지사항_전체조회_빈목록")
    void getNotices_empty() throws Exception {
        when(noticeService.getAllNotices()).thenReturn(List.of());

        mockMvc.perform(get("/notices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(noticeService).getAllNotices();
    }

    /* =============== GET /notices/{id} =============== */

    @Test
    @WithMockUser
    @DisplayName("공지사항_단건조회_성공")
    void getNotice_success() throws Exception {
        Long noticeId = 1L;
        LocalDateTime now = LocalDateTime.now();
        NoticeDetailResponseDto dto = noticeDetailDto(noticeId, "테스트 공지사항", "테스트 내용", now);

        when(noticeService.getNoticeById(noticeId)).thenReturn(dto);

        mockMvc.perform(get("/notices/{id}", noticeId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.noticeId").value(noticeId))
            .andExpect(jsonPath("$.noticeTitle").value("테스트 공지사항"))
            .andExpect(jsonPath("$.noticeContent").value("테스트 내용"));

        verify(noticeService).getNoticeById(noticeId);
    }

    @Test
    @WithMockUser
    @DisplayName("공지사항_단건조회_실패_공지사항없음")
    void getNotice_fail_notFound() throws Exception {
        Long noticeId = 999L;
        when(noticeService.getNoticeById(noticeId)).thenThrow(new NoticeNotFoundException(noticeId));

        mockMvc.perform(get("/notices/{id}", noticeId))
            .andExpect(status().isNotFound());

        verify(noticeService).getNoticeById(noticeId);
    }

    /* =============== POST /notices =============== */

    @Test
    @DisplayName("공지사항_등록_성공")
    void createNotice_success() throws Exception {
        stubAdminAuth();
        doNothing().when(noticeService).createNotice(any(NoticeRequestDto.class), eq(1L));

        mockMvc.perform(post("/notices")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "새 공지사항",
                        "content": "새 내용입니다."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("공지사항이 등록되었습니다."));

        verify(noticeService).createNotice(any(NoticeRequestDto.class), eq(1L));
    }

    @Test
    @DisplayName("공지사항_등록_실패_유효성검증_제목빈값")
    void createNotice_fail_validation_blankTitle() throws Exception {
        stubAdminAuth();

        mockMvc.perform(post("/notices")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "",
                        "content": "내용입니다."
                    }
                    """))
            .andExpect(status().isBadRequest());

        verify(noticeService, never()).createNotice(any(), any());
    }

    @Test
    @DisplayName("공지사항_등록_실패_유효성검증_내용빈값")
    void createNotice_fail_validation_blankContent() throws Exception {
        stubAdminAuth();

        mockMvc.perform(post("/notices")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "제목입니다.",
                        "content": ""
                    }
                    """))
            .andExpect(status().isBadRequest());

        verify(noticeService, never()).createNotice(any(), any());
    }

    /* =============== PUT /notices/{id} =============== */

    @Test
    @DisplayName("공지사항_수정_성공")
    void updateNotice_success() throws Exception {
        Long noticeId = 1L;
        stubAdminAuth();
        doNothing().when(noticeService).updateNotice(eq(noticeId), any(NoticeRequestDto.class), eq(1L));

        mockMvc.perform(put("/notices/{id}", noticeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "수정된 제목",
                        "content": "수정된 내용입니다."
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("공지사항이 수정되었습니다."));

        verify(noticeService).updateNotice(eq(noticeId), any(NoticeRequestDto.class), eq(1L));
    }

    @Test
    @DisplayName("공지사항_수정_실패_공지사항없음")
    void updateNotice_fail_notFound() throws Exception {
        Long noticeId = 999L;
        stubAdminAuth();
        doThrow(new NoticeNotFoundException(noticeId))
            .when(noticeService).updateNotice(eq(noticeId), any(NoticeRequestDto.class), eq(1L));

        mockMvc.perform(put("/notices/{id}", noticeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "수정된 제목",
                        "content": "수정된 내용입니다."
                    }
                    """))
            .andExpect(status().isNotFound());

        verify(noticeService).updateNotice(eq(noticeId), any(NoticeRequestDto.class), eq(1L));
    }

    @Test
    @DisplayName("공지사항_수정_실패_유효성검증")
    void updateNotice_fail_validation() throws Exception {
        Long noticeId = 1L;
        stubAdminAuth();

        mockMvc.perform(put("/notices/{id}", noticeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "",
                        "content": ""
                    }
                    """))
            .andExpect(status().isBadRequest());

        verify(noticeService, never()).updateNotice(any(), any(), any());
    }

    /* =============== DELETE /notices/{id} =============== */

    @Test
    @DisplayName("공지사항_삭제_성공")
    void deleteNotice_success() throws Exception {
        Long noticeId = 1L;
        stubAdminAuth();
        doNothing().when(noticeService).deleteNotice(eq(noticeId), eq(1L));

        mockMvc.perform(delete("/notices/{id}", noticeId)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("공지사항이 삭제되었습니다."));

        verify(noticeService).deleteNotice(eq(noticeId), eq(1L));
    }

    @Test
    @DisplayName("공지사항_삭제_실패_공지사항없음")
    void deleteNotice_fail_notFound() throws Exception {
        Long noticeId = 999L;
        stubAdminAuth();
        doThrow(new NoticeNotFoundException(noticeId))
            .when(noticeService).deleteNotice(eq(noticeId), eq(1L));

        mockMvc.perform(delete("/notices/{id}", noticeId)
                .with(csrf()))
            .andExpect(status().isNotFound());

        verify(noticeService).deleteNotice(eq(noticeId), eq(1L));
    }
}
