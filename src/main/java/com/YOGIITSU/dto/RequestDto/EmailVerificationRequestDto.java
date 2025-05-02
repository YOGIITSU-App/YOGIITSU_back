package com.YOGIITSU.dto.RequestDto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationRequestDto {

    private String email;
    private String code;
}