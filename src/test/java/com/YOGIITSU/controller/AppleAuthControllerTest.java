package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.TokenResponseDto;
import com.YOGIITSU.dto.ResponseDto.UserResponseDto;
import com.YOGIITSU.service.AppleAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppleAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AppleAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppleAuthService appleAuthService;

    /* ================= Success: 애플로그인 성공 ================= */
    @DisplayName("애플로그인_성공")
    @Test
    void loginWithApple_success() throws Exception {
        // given
        UserResponseDto user = UserResponseDto.builder()
            .id(1L)
            .role("ROLE_USER")
            .build();

        TokenResponseDto tokenResponse = TokenResponseDto.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .user(user)
            .build();

        when(appleAuthService.loginWithApple("valid-authorization-code"))
            .thenReturn(tokenResponse);

        String requestBody = """
                { "authorizationCode": "valid-authorization-code" }
            """;

        // when & then
        mockMvc.perform(post("/auth/apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(header().string("X-Refresh-Token", "refresh-token"))
            .andExpect(jsonPath("$.message").value("로그인 성공"))
            .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(appleAuthService).loginWithApple("valid-authorization-code");
    }

    /* ================= FAIL: 애플로그인 실패 ================= */
    @DisplayName("애플로그인_실패_코드공백")
    @Test
    void loginWithApple_blankCode() throws Exception {
        String requestBody = """
                { "authorizationCode": "" }
            """;

        mockMvc.perform(post("/auth/apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(appleAuthService);
    }

    @DisplayName("애플로그인_실패_코드누락")
    @Test
    void loginWithApple_missingCode() throws Exception {
        String requestBody = """
                { }
            """;

        mockMvc.perform(post("/auth/apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(appleAuthService);
    }
}
