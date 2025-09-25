package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.MemberNotFoundException;
import com.YOGIITSU.dto.ResponseDto.MypageProfileResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MypageProfileService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MypageProfileResponseDto getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        return MypageProfileResponseDto.builder()
            .memberId(member.getMemberId())
            .userName(member.getUserName())
            .email(member.getEmail())
            .build();
    }
}
