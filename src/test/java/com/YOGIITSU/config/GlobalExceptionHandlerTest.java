package com.YOGIITSU.config;

import com.YOGIITSU.config.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@RestController
	static class TestController {
		@GetMapping("/test")
		public String get() {
			return "ok";
		}
	}

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	@DisplayName("존재하지_않는_경로_요청시_404_응답")
	void noHandlerFound_returns404() throws Exception {
		mockMvc.perform(get("/nonexistent-path"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("RESOURCE_008"))
			.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@DisplayName("지원하지_않는_메서드_요청시_405_응답")
	void methodNotSupported_returns405() throws Exception {
		mockMvc.perform(delete("/test"))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(header().string("Allow", "GET"))
			.andExpect(jsonPath("$.code").value("VALIDATION_009"))
			.andExpect(jsonPath("$.status").value(405));
	}
}
