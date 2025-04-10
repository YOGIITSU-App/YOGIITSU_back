package com.YOGIITSU.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailChangeConfirmRequestDto {

    private String newEmail;
    private String token;
    private String code;
}

