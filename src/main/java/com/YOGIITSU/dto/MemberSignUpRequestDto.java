package com.YOGIITSU.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor
public class MemberSignUpRequestDto {

    @NotBlank(message = "Please enter your ID.") // 값이 비어있거나 공백일 경우 유효성 검사 실패
    @Size(min = 3, max = 50, message = "ID must be between 3 and 50 characters.") // 길이 제한
    private String memberId;

    // 비밀번호는 공백일 수 없으며, 최소 8자 이상이어야 함
    @NotBlank(message = "Please enter your password.") // 값이 비어있거나 공백일 경우 유효성 검사 실패
    @Size(min = 8, message = "Password must be at least 8 characters long.") // 비밀번호 8자 이상이어야 함
    private String password;

    // 이메일은 공백일 수 없으며, 유효한 이메일 형식이어야 함
    @NotBlank(message = "Please enter your email.")
    @Email(message = "Please enter a valid email address.") // 이메일 형식 검증
    private String email;

    // 사용자 이름 공백일 수 없음
    @NotBlank(message = "Please enter your username.")
    private String userName;
}