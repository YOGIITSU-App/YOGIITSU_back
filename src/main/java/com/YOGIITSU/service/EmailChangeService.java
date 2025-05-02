package com.YOGIITSU.service;

import com.YOGIITSU.dto.RequestDto.EmailChangeConfirmRequestDto;
import com.YOGIITSU.entity.EmailMessage;
import com.YOGIITSU.entity.Member;
import com.YOGIITSU.repository.EmailMessageRepository;
import com.YOGIITSU.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailChangeService {

    private final EmailMessageRepository emailMessageRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void changeEmail(String memberId, EmailChangeConfirmRequestDto dto) {
        // DB에서 이메일 + 코드로 검색
        EmailMessage emailMessage = emailMessageRepository.findByEmailAndCode(dto.getNewEmail(),
                dto.getCode())
            .orElseThrow(() -> new IllegalArgumentException("DB에 인증 코드가 없습니다."));

        // 만료 확인
        if (emailMessage.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        // 승인 처리
        emailMessage.setIsApproved(true);
        emailMessageRepository.save(emailMessage);

        // 회원 이메일 변경
        Member member = memberRepository.findByMemberId(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        member.setEmail(dto.getNewEmail());
        memberRepository.save(member);
    }
}