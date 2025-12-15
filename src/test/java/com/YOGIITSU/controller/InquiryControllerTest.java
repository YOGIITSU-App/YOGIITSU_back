package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
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
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.repository.MemberRepository;
import com.YOGIITSU.service.InquiryService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = InquiryController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
@Import(GlobalExceptionHandler.class)
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InquiryService inquiryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private MemberRepository memberRepository;

    /* =============== helpers: auth stubbing =============== */

    private void stubAuthAsMember(Long memberId) {
        String token = "dummy-token";
        when(jwtTokenProvider.resolveToken(any())).thenReturn(token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(String.valueOf(memberId));
        when(jwtTokenProvider.getAuthentication(token)).thenReturn(auth);

        Member member = Member.builder().id(memberId).userName("테스트").build();
        when(memberRepository.findByMemberId(String.valueOf(memberId)))
            .thenReturn(Optional.of(member));
    }

    private void stubAuthAsGuest() {
        when(jwtTokenProvider.resolveToken(any())).thenReturn(null);
    }

    /* ================= CREATE: 문의 등록 ================= */

    @DisplayName("문의등록_성공_회원")
    @Test
    void createInquiry_success_member() throws Exception {
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        doNothing().when(inquiryService).createInquiry(any(InquiryRequestDto.class), eq(memberId));

        mockMvc.perform(post("/inquiries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle": "테스트 제목",
                        "inquiryContent": "테스트 내용"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(content().string("등록이 완료되었습니다."));
    }

    @DisplayName("문의등록_실패_비회원")
    @Test
    void createInquiry_fail_guest() throws Exception {
        stubAuthAsGuest();

        mockMvc.perform(post("/inquiries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle": "테스트 제목",
                        "inquiryContent": "테스트 내용"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("문의등록_실패_유효하지않은토큰")
    @Test
    void createInquiry_fail_invalidToken() throws Exception {
        String invalidToken = "invalid-token";
        when(jwtTokenProvider.resolveToken(any())).thenReturn(invalidToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        mockMvc.perform(post("/inquiries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle": "테스트 제목",
                        "inquiryContent": "테스트 내용"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }

    @DisplayName("문의등록_실패_회원정보없음")
    @Test
    void createInquiry_fail_memberNotFound() throws Exception {
        String token = "valid-token";
        String memberIdStr = "999";

        when(jwtTokenProvider.resolveToken(any())).thenReturn(token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(memberIdStr);
        when(jwtTokenProvider.getAuthentication(token)).thenReturn(auth);

        // DB에서 찾을 수 없음 -> Optional.empty()
        when(memberRepository.findByMemberId(memberIdStr)).thenReturn(Optional.empty());

        mockMvc.perform(post("/inquiries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle": "테스트 제목",
                        "inquiryContent": "테스트 내용"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @DisplayName("문의등록_실패_유효성오류")
    @Test
    void createInquiry_fail_validation() throws Exception {
        stubAuthAsMember(1L);

        mockMvc.perform(post("/inquiries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle":"",
                        "inquiryContent":""
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    /* ================= READ-LIST: 문의 전체 리스트 조회 ================= */

    @DisplayName("문의리스트조회_성공_회원")
    @Test
    void getAllInquiries_success_member() throws Exception {
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        Inquiry inquiry1 = inquiry(1L, "문의1", "내용1", "김보통", InquiryState.PROCESSING);
        Inquiry inquiry2 = inquiry(2L, "문의2", "내용2", "이진짜", InquiryState.COMPLETED);

        when(inquiryService.getAllInquiries(memberId))
            .thenReturn(List.of(
                new InquiryListResponseDto(inquiry1, true),
                new InquiryListResponseDto(inquiry2, false)
            ));

        mockMvc.perform(get("/inquiries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].inquiryTitle").value("문의1"))
            .andExpect(jsonPath("$[0].isMine").value(true))
            .andExpect(jsonPath("$[1].inquiryTitle").value("문의2"))
            .andExpect(jsonPath("$[1].isMine").value(false));
    }

    @DisplayName("문의리스트조회_성공_비회원")
    @Test
    void getAllInquiries_success_guest() throws Exception {
        stubAuthAsGuest();

        Inquiry inquiry = inquiry(1L, "문의1", "내용1", "김진짜", InquiryState.PROCESSING);
        when(inquiryService.getAllInquiries(null))
            .thenReturn(List.of(new InquiryListResponseDto(inquiry, false)));

        mockMvc.perform(get("/inquiries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].inquiryTitle").value("문의1"))
            .andExpect(jsonPath("$[0].isMine").value(false));
    }

    @DisplayName("문의리스트조회_성공_유효하지않은토큰_비회원처리")
    @Test
    void getAllInquiries_success_invalidToken_treatedAsGuest() throws Exception {
        String invalidToken = "invalid-token";

        when(jwtTokenProvider.resolveToken(any())).thenReturn(invalidToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        Inquiry inquiry = inquiry(1L, "문의1", "내용1", "김진짜", InquiryState.PROCESSING);
        when(inquiryService.getAllInquiries(null)) // null(비회원)로 호출되는지 검증
            .thenReturn(List.of(new InquiryListResponseDto(inquiry, false)));
        
        mockMvc.perform(get("/inquiries")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    /* ================= READ-DETAIL: 문의 상세 조회 ================= */

    @DisplayName("문의상세조회_성공_회원")
    @Test
    void getInquiry_success_member() throws Exception {
        Long inquiryId = 1L;
        Long memberId = 1L;

        stubAuthAsMember(memberId);

        InquiryResponseDto dto = new InquiryResponseDto(
            inquiryId, "문의 제목", "문의 내용",
            LocalDateTime.now(), InquiryState.PROCESSING,
            "홍길동", null, null, null, true
        );

        when(inquiryService.getInquiry(inquiryId, memberId)).thenReturn(dto);

        mockMvc.perform(get("/inquiries/{inquiryId}", inquiryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.inquiryTitle").value("문의 제목"))
            .andExpect(jsonPath("$.isMine").value(true));
    }

    @DisplayName("문의상세조회_성공_비회원")
    @Test
    void getInquiry_success_guest() throws Exception {
        Long inquiryId = 1L;
        stubAuthAsGuest();

        Inquiry inquiry = inquiry(inquiryId, "제목", "내용", "김보통", InquiryState.PROCESSING);
        when(inquiryService.getInquiry(inquiryId, null))
            .thenReturn(new InquiryResponseDto(inquiry, false));

        mockMvc.perform(get("/inquiries/{inquiryId}", inquiryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isMine").value(false));
    }

    @DisplayName("문의상세조회_실패_존재하지않음")
    @Test
    void getInquiry_fail_notFound() throws Exception {
        Long inquiryId = 999L;
        stubAuthAsGuest();

        when(inquiryService.getInquiry(inquiryId, null)).thenThrow(new InquiryNotFoundException());

        mockMvc.perform(get("/inquiries/{inquiryId}", inquiryId))
            .andExpect(status().isNotFound());
    }

    /* ================= UPDATE: 문의 수정 ================= */

    @DisplayName("문의수정_성공")
    @Test
    void updateInquiry_success() throws Exception {
        Long inquiryId = 1L;
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        Inquiry inquiry = inquiry(inquiryId, "수정전", "수정전", "김보통",
            InquiryState.PROCESSING);
        InquiryResponseDto updated = new InquiryResponseDto(inquiry, true);

        when(
            inquiryService.updateInquiry(eq(inquiryId), eq(memberId), any(InquiryRequestDto.class)))
            .thenReturn(updated);

        mockMvc.perform(put("/inquiries/{inquiryId}", inquiryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle":"새 제목",
                        "inquiryContent":"새 내용"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isMine").value(true));
    }

    @DisplayName("문의수정_실패_내문의아님")
    @Test
    void updateInquiry_fail_notOwner() throws Exception {
        Long inquiryId = 1L;
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        when(
            inquiryService.updateInquiry(eq(inquiryId), eq(memberId), any(InquiryRequestDto.class)))
            .thenThrow(new AccessDeniedException(""));

        mockMvc.perform(put("/inquiries/{inquiryId}", inquiryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle":"새 제목","inquiryContent":"새 내용"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @DisplayName("문의수정_실패_답변완료")
    @Test
    void updateInquiry_fail_completed() throws Exception {
        Long inquiryId = 1L;
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        when(
            inquiryService.updateInquiry(eq(inquiryId), eq(memberId), any(InquiryRequestDto.class)))
            .thenThrow(new InvalidInquiryStateException(""));

        mockMvc.perform(put("/inquiries/{inquiryId}", inquiryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle":"새 제목","inquiryContent":"새 내용"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @DisplayName("문의수정_실패_변경사항없음")
    @Test
    void updateInquiry_fail_noChanges() throws Exception {
        Long inquiryId = 1L;
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        when(
            inquiryService.updateInquiry(eq(inquiryId), eq(memberId), any(InquiryRequestDto.class)))
            .thenThrow(new MissingRequiredFieldException(""));

        mockMvc.perform(put("/inquiries/{inquiryId}", inquiryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "inquiryTitle":null,"inquiryContent":null
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    /* ================= DELETE ================= */

    @DisplayName("문의삭제_성공")
    @Test
    void deleteInquiry_success() throws Exception {
        Long inquiryId = 1L;
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        doNothing().when(inquiryService).deleteInquiry(inquiryId, memberId);

        mockMvc.perform(delete("/inquiries/{inquiryId}", inquiryId))
            .andExpect(status().isNoContent());
    }

    @DisplayName("문의삭제_실패_내문의아님")
    @Test
    void deleteInquiry_fail_notOwner() throws Exception {
        Long inquiryId = 1L;
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        doThrow(new AccessDeniedException(""))
            .when(inquiryService).deleteInquiry(inquiryId, memberId);

        mockMvc.perform(delete("/inquiries/{inquiryId}", inquiryId))
            .andExpect(status().isForbidden());
    }

    @DisplayName("문의삭제_실패_답변완료")
    @Test
    void deleteInquiry_fail_completed() throws Exception {
        Long inquiryId = 1L;
        Long memberId = 1L;
        stubAuthAsMember(memberId);

        doThrow(new InvalidInquiryStateException(""))
            .when(inquiryService).deleteInquiry(inquiryId, memberId);

        mockMvc.perform(delete("/inquiries/{inquiryId}", inquiryId))
            .andExpect(status().isBadRequest());
    }

    /* ================= Dummy method ================= */

    private Inquiry inquiry(Long id, String inquiryTitle, String inquiryContent,
        String authorName, InquiryState inquiryState) {
        Member member = Member.builder()
            .id(1L)
            .userName(authorName)
            .build();

        return Inquiry.builder()
            .inquiryId(id)
            .member(member)
            .inquiryTitle(inquiryTitle)
            .inquiryContent(inquiryContent)
            .inquiryAt(LocalDateTime.now())
            .inquiryState(inquiryState)
            .build();
    }
}
