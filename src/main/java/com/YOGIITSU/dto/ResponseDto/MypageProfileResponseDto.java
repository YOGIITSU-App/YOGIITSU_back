package com.YOGIITSU.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MypageProfileResponseDto {

    private String userName;  // 사용자 이름
    private String email;  // 이메일
    private String memberId;  // 로그인 ID
}
