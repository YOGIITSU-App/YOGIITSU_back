package com.YOGIITSU.service;

import com.YOGIITSU.dto.ResponseDto.ChatNodeResponseDto;

public interface ChatbotService {

	ChatNodeResponseDto getNode(Long nodeId,
		Long collegeId,
		Long deptId);
}