package com.YOGIITSU.controller;

import com.YOGIITSU.dto.RequestDto.MemberSignUpRequestDto;
import com.YOGIITSU.service.SignUpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignUpController.class)
@AutoConfigureMockMvc(addFilters = false)
class SignUpControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	SignUpService signUpService;

	@Test
	@DisplayName("회원가입 API 성공")
	void signup_success() throws Exception {
		MemberSignUpRequestDto dto =
			new MemberSignUpRequestDto("testuser", "Test1234!", "test@suwon.ac.kr", "홍길동");

		doNothing().when(signUpService).register(dto);

		mockMvc.perform(post("/members/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("회원가입 API 실패")
	void signup_validation_fail() throws Exception {
		MemberSignUpRequestDto invalid = new MemberSignUpRequestDto();

		mockMvc.perform(post("/members/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalid)))
			.andExpect(status().isBadRequest());
	}
}
