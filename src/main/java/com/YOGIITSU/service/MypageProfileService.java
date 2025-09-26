package com.YOGIITSU.service;

import com.YOGIITSU.config.handler.GlobalExceptionHandler.MemberNotFoundException;
import com.YOGIITSU.dto.ResponseDto.MypageProfileResponseDto;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MypageProfileService
 * - 마이페이지 프로필 조회 비즈니스 로직 처리
 * - DB에서 사용자 정보를 조회하여 DTO로 반환
 */
@Service
@RequiredArgsConstructor
public class MypageProfileService {

    private final MemberRepository memberRepository;

    /**
     * 마이페이지 프로필 조회
     * - memberId 기준으로 사용자 엔티티 조회
     * - 존재하지 않는 경우 MemberNotFoundException 발생
     * - 조회한 엔티티를 MypageProfileResponseDto로 변환 후 반환
     *
     * @param memberId 사용자 고유 ID (PK)
     * @return MypageProfileResponseDto 사용자 프로필 응답 DTO
     */
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
