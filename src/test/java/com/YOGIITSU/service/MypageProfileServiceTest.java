package com.YOGIITSU.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.YOGIITSU.dto.ResponseDto.MypageProfileResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.exception.user.MemberNotFoundException;
import com.YOGIITSU.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class MypageProfileServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MypageProfileService mypageProfileService;

    /* ================= READ: 마이페이지 프로필 조회 ================= */
    @DisplayName("마이페이지프로필조회_성공")
    @Test
    void getProfile_success() {
        Long id = 1L;
        Member member = createMember(id, "testMemberId", "김보통", "normal@test.com");

        when(memberRepository.findById(id)).thenReturn(Optional.of(member));

        MypageProfileResponseDto result = mypageProfileService.getProfile(id);

        assertNotNull(result);

        assertEquals("testMemberId", result.getMemberId());
        assertEquals("김보통", result.getUserName());
        assertEquals("normal@test.com", result.getEmail());

        verify(memberRepository).findById(id);
    }

    @DisplayName("마이페이지프로필조회_실패_존재하지않음")
    @Test
    void getProfile_fail_notFound() {
        Long id = 999L;
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () ->
            mypageProfileService.getProfile(id));

        verify(memberRepository).findById(id);
    }

    /* ================= Dummy method ================= */
    private Member createMember(Long id, String memberId, String userName, String email) {
        Member member = Member.builder()
            .memberId(memberId)
            .userName(userName)
            .email(email)
            .password("pw")
            .role("ROLE_USER")
            .build();

        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

}
