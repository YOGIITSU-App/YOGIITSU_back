package com.YOGIITSU.dto.RequestDto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppleLoginRequestDto {

    @NotBlank(message = "authorizationCode은 필수입니다.")
    private String authorizationCode;

    @JsonCreator
    public AppleLoginRequestDto(@JsonProperty("authorizationCode") String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }
}
