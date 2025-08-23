package com.YOGIITSU.controller;

import com.YOGIITSU.dto.ResponseDto.ChatNodeResponseDto;
import com.YOGIITSU.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

@Tag(name = "챗봇 API", description = "사용자 선택에 따라 DB 데이터를 조회하여 답변을 생성합니다.")
@Validated
@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

	private final ChatbotService chatbotService;

	// buildingId, collegeName, deptName 은 필요할 때만 전달
	@Operation(summary = "챗봇 응답 생성", description = "사용자 입력(선택지 ID 등)에 따라 관련 정보를 조회하고, GPT를 통해 자연스러운 답변을 반환합니다.")
	@GetMapping("/node/{optionid}")
	public ResponseEntity<ChatNodeResponseDto> getNode(
		@PathVariable("optionid") @Positive Long id,
		@RequestParam(name = "collegeId", required = false) @Positive Long collegeId,
		@RequestParam(name = "deptId", required = false) @Positive Long deptId
	) {
		return ResponseEntity.ok(
			chatbotService.getNode(id, collegeId, deptId)
		);
	}
}