package com.YOGIITSU.dto.RequestDto;

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
    @Size(min = 4, max = 20, message = "아이디는 4~20자의 영문 또는 영문+숫자로만 구성되어야 합니다.")
    private String memberId;

    // 비밀번호는 공백일 수 없으며, 최소 8자 이상이어야 함
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하이어야 합니다.") // 비밀번호 8자 이상이어야 함
    private String password;

    // 이메일은 공백일 수 없으며, 유효한 이메일 형식이어야 함
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;

    // 사용자 이름 공백일 수 없음 + 최소 2글자 이상이어야 함
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 2, max = 8, message = "이름은 2~8자의 영문, 한글, 숫자로만 입력해주세요. 특수문자는 사용할 수 없습니다.")
    private String userName;
}