package com.YOGIITSU.controller;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
import com.YOGIITSU.dto.ResponseDto.MypageProfileResponseDto;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.jwt.CustomUserDetails;
import com.YOGIITSU.jwt.JwtTokenProvider;
import com.YOGIITSU.service.MypageProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = MypageProfileController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    }
)
@Import({GlobalExceptionHandler.class, MypageProfileControllerTest.TestMvcConfig.class})
class MypageProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MypageProfileService mypageProfileService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @TestConfiguration
    static class TestMvcConfig implements WebMvcConfigurer {

        @Autowired
        private JwtTokenProvider jwtTokenProvider;

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(
                    org.springframework.core.MethodParameter parameter) {
                    return CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
                }

                @Override
                public Object resolveArgument(
                    org.springframework.core.MethodParameter parameter,
                    ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest,
                    WebDataBinderFactory binderFactory
                ) {
                    HttpServletRequest request = webRequest.getNativeRequest(
                        HttpServletRequest.class);
                    if (request == null) {
                        return null;
                    }

                    String token = jwtTokenProvider.resolveToken(request);
                    if (token == null) {
                        return null;
                    }

                    if (!jwtTokenProvider.validateToken(token)) {
                        return null;
                    }

                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    if (authentication == null) {
                        return null;
                    }

                    Object principal = authentication.getPrincipal();
                    return (principal instanceof CustomUserDetails) ? principal : null;
                }
            });
        }
    }

    /* ================= READ: 마이페이지 프로필 조회 ================= */
    @DisplayName("마이페이지프로필조회_성공")
    @Test
    void getMyProfile_success() throws Exception {
        Long memberId = 1L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(memberId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = "validToken";

        when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(token)).thenReturn(authentication);

        MypageProfileResponseDto response = MypageProfileResponseDto.builder()
            .memberId("testMemberId")
            .userName("김보통")
            .email("normal@test.com")
            .build();

        when(mypageProfileService.getProfile(memberId)).thenReturn(response);

        mockMvc.perform(get("/mypage/profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memberId").value("testMemberId"))
            .andExpect(jsonPath("$.userName").value("김보통"))
            .andExpect(jsonPath("$.email").value("normal@test.com"));

        verify(mypageProfileService).getProfile(memberId);
    }

    @DisplayName("마이페이지프로필조회_실패_토큰없음")
    @Test
    void getMyProfile_fail_missingToken() throws Exception {
        when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(get("/mypage/profile"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("AUTH_004"));

        verify(mypageProfileService, never()).getProfile(anyLong());
    }

    @DisplayName("마이페이지프로필조회_실패_회원없음")
    @Test
    void getMyProfile_fail_memberNotFound() throws Exception {
        Long memberId = 999L;

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(memberId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = "validToken";

        when(jwtTokenProvider.resolveToken(any(HttpServletRequest.class))).thenReturn(token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(token)).thenReturn(authentication);

        when(mypageProfileService.getProfile(memberId))
            .thenThrow(new MemberNotFoundException());

        mockMvc.perform(get("/mypage/profile"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("USER_001"));

        verify(mypageProfileService).getProfile(memberId);
    }
}
