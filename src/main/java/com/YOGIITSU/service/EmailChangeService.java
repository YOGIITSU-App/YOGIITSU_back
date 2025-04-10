package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.EmailChangeConfirmRequestDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.jwt.EmailVerificationJwtProvider;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailChangeService {

    private final EmailVerificationJwtProvider emailJwtProvider;
    private final EmailMessageRepository emailMessageRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void changeEmail(String memberId, EmailChangeConfirmRequestDto dto) {
        Claims claims = emailJwtProvider.parseEmailToken(dto.getToken())
            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        String tokenEmail = claims.getSubject();
        String tokenCode = claims.get("code", String.class);

        if (!tokenEmail.equals(dto.getNewEmail()) || !tokenCode.equals(dto.getCode())) {
            throw new IllegalArgumentException("인증 실패: 이메일 또는 코드가 일치하지 않습니다.");
        }

        EmailMessage emailMessage = emailMessageRepository
            .findByEmailAndCode(dto.getNewEmail(), dto.getCode())
            .orElseThrow(() -> new IllegalArgumentException("DB에 인증 코드가 없습니다."));

        if (emailMessage.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        emailMessage.setIsApproved(true); // 인증 완료
        emailMessageRepository.save(emailMessage);

        // 실제 이메일 변경
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        member.setEmail(dto.getNewEmail());
        memberRepository.save(member);
    }
}

