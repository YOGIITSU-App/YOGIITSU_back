package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MypageProfileResponseDto {

    private String memberId;  // 로그인 ID
    private String userName;  // 사용자 이름
    private String email;  // 이메일
}
