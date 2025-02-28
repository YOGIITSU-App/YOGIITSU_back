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

    @NotBlank(message = "아이디를 입력해주세요.") // 값이 비어있거나 공백일 경우 유효성 검사 실패
    @Size(min = 3, max = 50, message = "아이디는 3자 이상 50자 이하로 입력해주세요.")
    private String memberId;

    // 비밀번호는 공백일 수 없으며, 최소 8자 이상이어야 함
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.") // 비밀번호 8자 이상이어야 함
    private String password;

    // 이메일은 공백일 수 없으며, 유효한 이메일 형식이어야 함
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;

    // 사용자 이름 공백일 수 없음 + 최소 2글자 이상이어야 함
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 2, message = "이름은 최소 2글자 이상이어야 합니다.")
    private String userName;
}