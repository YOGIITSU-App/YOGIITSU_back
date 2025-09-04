package com.YOGIITSU.dto.RequestDto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AppleLoginRequestDto {

    @NotBlank(message = "identityToken은 필수입니다.")
    private String identityToken;

    @Size(max = 128)
    private String rawNonce;

    @JsonCreator
    public AppleLoginRequestDto(@JsonProperty("identityToken") String identityToken,
        @JsonProperty("rawNonce") String rawNonce) {
        this.identityToken = identityToken;
        this.rawNonce = rawNonce;
    }
}
