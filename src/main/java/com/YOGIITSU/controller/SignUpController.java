package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.MemberSignUpRequestDto;
import com.YOGIITSU.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 회원가입과 관련된 요청을 처리하는 API
 */
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpService signUpService;

    @PostMapping("/signup")
    public Map<String, String> signUp(
        @RequestBody @Valid MemberSignUpRequestDto memberSignUpRequestDto) {
        return signUpService.register(memberSignUpRequestDto);
    }
}
