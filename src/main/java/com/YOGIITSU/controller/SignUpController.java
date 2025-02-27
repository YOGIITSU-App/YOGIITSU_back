package com.YOGIITSU.controller;

import com.YOGIITSU.dto.MemberSignUpRequestDto;
import com.YOGIITSU.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SignUpController는 회원가입과 관련된 요청을 처리하는 REST API 컨트롤러다
 */

@RestController
@RequestMapping("/member") // "/member" 경로로 들어오는 요청을 처리
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpService signUpService; // 회원가입 관련 로직을 처리하는 서비스다

    @PostMapping("/signup") // "/member/signup" 경로로 POST 요청이 들어올 때 호출
    public ResponseEntity<String> signUp(@RequestBody @Valid MemberSignUpRequestDto memberSignUpRequestDto) {
        signUpService.register(memberSignUpRequestDto);
        return ResponseEntity.ok("회원가입이 성공적으로 되셨습니다!");
        // 성공적으로 회원가입이 완료되었음을 클라이언트에 반환
    }
}